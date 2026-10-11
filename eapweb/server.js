const express = require('express');
const PDFDocument = require('pdfkit');
const crypto = require('crypto');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 10000;
const APPS_SCRIPT_URL = process.env.APPS_SCRIPT_URL || '';
const API_TOKEN = process.env.API_TOKEN || '';

app.use(express.json({limit:'35mb'}));
app.use(express.static(path.join(__dirname,'public'), {
  setHeaders(res, filePath) {
    if (filePath.endsWith('sw.js')) res.setHeader('Cache-Control','no-cache');
  }
}));

app.get('/api/health', (_req,res) => {
  res.json({ok:true, backendConfigured:Boolean(APPS_SCRIPT_URL && API_TOKEN)});
});

app.post('/api/status', async (req,res) => {
  try {
    const result = await callAppsScript({
      action:'getStatus',
      projectLocation:req.body.projectLocation || '',
      locationCode:req.body.locationCode || 'NA'
    });
    res.status(result.success ? 200 : 400).json(result);
  } catch (e) {
    res.status(503).json({success:false,message:e.message || 'Central service unavailable'});
  }
});

app.post('/api/submit', async (req,res) => {
  try {
    const payload = {...(req.body || {})};
    payload.clientRequestId = payload.clientRequestId || crypto.randomUUID();
    if (!payload.name || !payload.projectLocation || !payload.locationCode) {
      return res.status(400).json({success:false,message:'Required registration details are missing.'});
    }

    let totalSerial = String(payload.totalSerialNumber || '').trim();
    let locationSerial = String(payload.individualSerialNumber || '').trim();

    if (!totalSerial || !locationSerial) {
      const reservation = await callAppsScript({
        action:'reserveSerial',
        clientRequestId:payload.clientRequestId,
        projectLocation:payload.projectLocation,
        locationCode:payload.locationCode,
        name:payload.name
      });
      if (!reservation.success) return res.status(409).json(reservation);
      totalSerial = reservation.totalSerialNumber;
      locationSerial = reservation.individualSerialNumber;
      payload.totalSerialNumber = totalSerial;
      payload.individualSerialNumber = locationSerial;
      payload.totalApplicationCount = reservation.totalApplicationCount || 0;
      payload.locationApplicationCount = reservation.locationApplicationCount || 0;
    }

    const pdf = await buildPdf(payload);
    payload.pdfBase64 = pdf.toString('base64');
    payload.pdfFileName = makePdfFileName(payload);
    payload.action = 'submitRegistration';

    const result = await callAppsScript(payload);
    if (!result.success) return res.status(400).json(result);

    res.json({
      ...result,
      totalSerialNumber: result.totalSerialNumber || totalSerial,
      individualSerialNumber: result.individualSerialNumber || locationSerial,
      pdfFileName: result.pdfFileName || payload.pdfFileName
    });
  } catch (e) {
    res.status(503).json({success:false,message:e.message || 'Submission failed'});
  }
});

app.get('*', (_req,res) => res.sendFile(path.join(__dirname,'public','index.html')));
app.listen(PORT, () => console.log('HAL EAP web app running on', PORT));

async function callAppsScript(payload) {
  if (!APPS_SCRIPT_URL || !API_TOKEN) throw new Error('Central EAP backend is not configured.');
  const body = new URLSearchParams({payload:JSON.stringify({...payload,token:API_TOKEN})});
  const response = await fetch(APPS_SCRIPT_URL, {
    method:'POST',
    headers:{'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'},
    body:body.toString(),
    redirect:'follow'
  });
  const text = await response.text();
  if (!response.ok) throw new Error('Central server HTTP ' + response.status);
  try { return JSON.parse(text); } catch (_) { throw new Error('Central server returned an invalid response.'); }
}

function buildPdf(d) {
  return new Promise((resolve,reject) => {
    try {
      const doc = new PDFDocument({size:'A4',margin:36,compress:true});
      const chunks=[]; doc.on('data',c=>chunks.push(c)); doc.on('end',()=>resolve(Buffer.concat(chunks))); doc.on('error',reject);

      doc.font('Helvetica-Bold').fontSize(14).text('ENTREPRENEURSHIP AWARENESS PROGRAMME (EAP)',{align:'center'});
      doc.font('Helvetica').fontSize(9).text('Organized by Hindustan Aeronautics Limited (HAL)',{align:'center'});
      doc.text('In Collaboration with Entrepreneurship Development Institute of India (EDII)',{align:'center'});
      doc.moveDown(.5);
      doc.font('Helvetica-Bold').fontSize(12).text('REGISTRATION FORM',{align:'center'});
      doc.moveDown(.7);

      const row=(label,value) => {
        doc.font('Helvetica-Bold').fontSize(9).text(label,{continued:true,width:170});
        doc.font('Helvetica').text(String(value||''));
        doc.moveDown(.25);
      };
      row('Date: ',d.eapDate); row('Place: ',d.place || d.city);
      row('Application Serial: ',d.totalSerialNumber); row('Location Serial: ',d.individualSerialNumber);
      doc.moveDown(.4);

      if (d.photoBase64) {
        try { doc.image(Buffer.from(String(d.photoBase64).replace(/^data:[^;]+;base64,/,''),'base64'),455,145,{fit:[90,110],align:'center',valign:'center'}); } catch(_){}
      }

      row('1. Full Name: ',d.name);
      row('2. Gender: ',d.gender);
      row('3. Date of Birth: ',d.dob + (d.ageOnEapDate ? '   Age: '+d.ageOnEapDate : ''));
      row("4. Father's / Husband's / Mother's Name: ",d.guardianName);
      row('5. Full Address: ',d.fullAddress);
      row('6. Mobile Phone: ',d.mobile);
      row('7. Alternative Phone No.: ',d.alternateMobile);
      row('8. Email ID: ',d.email);
      row('9. Aadhaar No.: ',d.idNumber);
      row('10. Highest Educational Qualification: ',d.education);
      row('11. Occupation: ',d.occupation);
      row('12. Income (Individual): ',d.individualIncome);
      row('13. Category: ',d.category);
      row('14. Intention for taking part in EAP: ',d.intention);
      row('15. Sector to start business: ',d.sector || d.sectors);
      row('16. Do you want to attend MSDP?: ',d.msdpInterest);

      doc.moveDown(.6);
      doc.font('Helvetica').fontSize(8.5).text('Declaration: I declare that the above information provided by me is completely correct. I will be responsible if any discrepancy is detected.');
      doc.moveDown(.8);
      doc.font('Helvetica-Bold').fontSize(9).text('Applicant Signature');
      if (d.signatureBase64) {
        try { doc.image(Buffer.from(String(d.signatureBase64).replace(/^data:[^;]+;base64,/,''),'base64'),390,650,{fit:[150,55],align:'center',valign:'center'}); } catch(_){}
      }
      doc.moveDown(4);
      doc.font('Helvetica').fontSize(7.5).text('Designed and Developed by Ajay Rao, EDII SRO Bengaluru',{align:'center'});
      doc.end();
    } catch(e) { reject(e); }
  });
}

function makePdfFileName(d) {
  const safe=s=>String(s||'').trim().replace(/\s+/g,'_').replace(/[\\/:*?"<>|#%]/g,'_').replace(/_+/g,'_').slice(0,100);
  const serial=safe(String(d.individualSerialNumber||'').replace(/\//g,'-'));
  return safe(d.name)+'_'+safe(d.place||d.city||'Location')+'_'+(serial||'Pending')+'.pdf';
}
