const express = require('express');
const PDFDocument = require('pdfkit');
const crypto = require('crypto');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 10000;
const APPS_SCRIPT_URL = process.env.APPS_SCRIPT_URL || '';
const API_TOKEN = process.env.API_TOKEN || '';
const HAL_FORM_LOGO = Buffer.from('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','base64');
const EDII_FORM_LOGO = Buffer.from('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','base64');

app.get('/downloads/HAL_EAP_Registration_iOS.mobileconfig',(req,res)=>{
  const file=path.join(__dirname,'public','downloads','HAL_EAP_Registration_iOS.mobileconfig');
  res.setHeader('Content-Type','application/x-apple-aspen-config');
  res.setHeader('Content-Disposition','attachment; filename="HAL_EAP_Registration_iOS.mobileconfig"');
  res.sendFile(file);
});

app.use(express.json({limit:'35mb'}));
app.use(express.static(path.join(__dirname,'public'), {
  setHeaders(res, filePath) {
    if (/(sw\.js|app\.js|styles\.css|index\.html)$/.test(filePath)) res.setHeader('Cache-Control','no-cache, no-store, must-revalidate');
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
    }, req.get('x-eap-token') || '');
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
      }, req.get('x-eap-token') || '');
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

    const result = await callAppsScript(payload, req.get('x-eap-token') || '');
    if (!result.success) return res.status(400).json(result);

    res.json({
      ...result,
      totalSerialNumber: result.totalSerialNumber || totalSerial,
      individualSerialNumber: result.individualSerialNumber || locationSerial,
      pdfFileName: result.pdfFileName || payload.pdfFileName,
      applicationPdfBase64: pdf.toString('base64'),
      applicationPdfMimeType: 'application/pdf'
    });
  } catch (e) {
    res.status(503).json({success:false,message:e.message || 'Submission failed'});
  }
});

app.post('/api/pdf', async (req,res) => {
  try {
    const payload = {...(req.body || {})};
    if (!payload.name) return res.status(400).json({success:false,message:'Applicant name is required to generate the PDF.'});
    const pdf = await buildPdf(payload);
    const fileName = makePdfFileName(payload);
    res.setHeader('Content-Type','application/pdf');
    res.setHeader('Content-Disposition','inline; filename="'+fileName.replace(/"/g,'')+'"');
    res.setHeader('Cache-Control','no-store');
    res.send(pdf);
  } catch (e) {
    res.status(500).json({success:false,message:e.message || 'Unable to generate application PDF.'});
  }
});

app.get('*', (_req,res) => res.sendFile(path.join(__dirname,'public','index.html')));
app.listen(PORT, () => {
  console.log('HAL EAP web app running on', PORT);
  setTimeout(async () => {
    try {
      const check = await callAppsScript({
        action:'getStatus',
        projectLocation:'Kolar | Mulbagal',
        locationCode:'K-M'
      });
      console.log('Central backend check:', check && check.success ? 'CONNECTED' : 'FAILED');
    } catch (e) {
      console.error('Central backend check: FAILED -', e.message);
    }
  }, 1500);
});

async function callAppsScript(payload, clientToken='') {
  const token = API_TOKEN || clientToken;
  if (!APPS_SCRIPT_URL || !token) throw new Error('Central EAP token is not configured on this device.');
  const body = new URLSearchParams({payload:JSON.stringify({...payload,token})});
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
      const doc = new PDFDocument({size:'A4',margin:0,compress:true,autoFirstPage:true});
      const chunks=[];
      doc.on('data',c=>chunks.push(c));
      doc.on('end',()=>resolve(Buffer.concat(chunks)));
      doc.on('error',reject);

      const pageW=595.28;
      const normal='Helvetica';
      const bold='Helvetica-Bold';
      const clean=v=>String(v==null?'':v).trim();
      const norm=v=>clean(v).toLowerCase().replace(/[^a-z0-9]+/g,'');
      const center=(text,y,size,font=normal,x=0,w=pageW)=>{
        doc.font(font).fontSize(size).fillColor('#000').text(text,x,y,{width:w,align:'center',lineBreak:false});
      };
      const fitSingle=(text,x,y,maxWidth,startSize=10.2,minSize=7.1,font=normal)=>{
        const value=clean(text);
        let size=startSize;
        doc.font(font).fontSize(size);
        while(size>minSize && doc.widthOfString(value)>maxWidth){
          size-=0.25;
          doc.fontSize(size);
        }
        doc.text(value,x,y,{width:maxWidth,lineBreak:false});
        return size;
      };
      const fitWrapped=(text,x,y,width,maxHeight,startSize=9.9,minSize=6.8,font=normal)=>{
        const value=clean(text);
        let size=startSize, h=0;
        while(size>=minSize){
          doc.font(font).fontSize(size);
          h=doc.heightOfString(value,{width,lineGap:0});
          if(h<=maxHeight) break;
          size-=0.25;
        }
        doc.font(font).fontSize(Math.max(size,minSize)).text(value,x,y,{width,lineGap:0});
        return Math.min(h,maxHeight);
      };
      const label=(num,text,y)=>{
        doc.font(normal).fontSize(11.6).fillColor('#000').text(num,89.3,y,{lineBreak:false});
        doc.text(text,143.3,y,{lineBreak:false});
      };
      const checkbox=(x,y,checked,size=11)=>{
        doc.lineWidth(0.8).rect(x,y,size,size).strokeColor('#000').stroke();
        if(checked){
          doc.lineWidth(1.5).moveTo(x+2.0,y+5.8).lineTo(x+4.7,y+8.4).lineTo(x+9.2,y+2.0).stroke();
        }
      };
      const choice=(xBox,xText,y,text,checked)=>{
        checkbox(xBox,y+0.4,checked,11);
        doc.font(normal).fontSize(11.6).fillColor('#000').text(text,xText,y,{lineBreak:false});
      };
      const imageBuffer=v=>{
        const raw=clean(v).replace(/^data:[^;]+;base64,/,'');
        return raw ? Buffer.from(raw,'base64') : null;
      };
      const drawImageFit=(buf,x,y,w,h)=>{
        if(!buf) return;
        try{doc.image(buf,x,y,{fit:[w,h],align:'center',valign:'center'});}catch(_){}
      };

      drawImageFit(HAL_FORM_LOGO,21.75,7.5,94.5,50.5);
      drawImageFit(EDII_FORM_LOGO,483.1,6.75,104.9,48.8);

      center('Entrepreneurship Awareness Programme (EAP)',8.5,14.0,bold,125,355);
      center('Organized by',26.5,12.0,normal,150,295);
      center('Hindustan Aeronautics Limited (HAL)',41.0,12.0,bold,150,295);
      center('In Collaboration with',56.2,12.0,normal,145,305);
      center('Entrepreneurship Development Institute of India (EDII)',71.0,12.0,bold,120,355);
      center('Website: www.ediindia.org, www. ediindia.ac.in',86.0,11.0,normal,140,315);

      center('*'.repeat(88),106.2,11.0,normal,78,442);
      center('Registration Form',121.5,12.0,bold,80,440);
      doc.font(normal).fontSize(11.6).text('Date:',83.1,137.2,{lineBreak:false});
      fitSingle(clean(d.eapDate),114.0,138.7,130,10.2,8.0,normal);
      doc.font(normal).fontSize(11.6).text('Place:',410.0,137.2,{lineBreak:false});
      fitSingle(clean(d.place||d.city),446.0,138.7,95,10.2,8.0,normal);
      center('*'.repeat(88),150.2,11.0,normal,78,442);

      const photo=imageBuffer(d.photoBase64);
      drawImageFit(photo,492.5,178.4,76.0,101.3);
      doc.lineWidth(0.5).rect(492.5,178.4,76.0,101.3).strokeColor('#000').stroke();

      label('1)','Full Name:',178.8);
      fitSingle(d.name,216.0,180.2,260,10.2,7.2,normal);

      label('2)','Gender:',204.8);
      fitSingle(d.gender,188.8,206.2,150,10.2,7.2,normal);

      label('3)','Date of Birth:',226.8);
      fitSingle(d.dob,229.0,228.2,175,10.2,7.2,normal);

      label('4)',"Father's/Husband's/Mother's Name:",248.8);
      fitSingle(d.guardianName,365.0,250.2,112,10.2,6.8,normal);

      label('5)','Full Address:',270.8);
      const addressHeight=fitWrapped(d.fullAddress,228.0,271.6,250,31,9.9,6.8,normal);
      const shift=Math.max(0,Math.min(20,addressHeight-11));

      const y6=292.8+shift;
      label('6)','Mobile Phone:',y6);
      fitSingle(d.mobile,227.6,y6+1.4,230,10.2,7.2,normal);

      const y7=314.8+shift;
      label('7)','Alternative Phone No.:',y7);
      fitSingle(d.alternateMobile,282.0,y7+1.4,185,10.2,7.2,normal);

      const y8=336.8+shift;
      label('8)','Email ID:',y8);
      fitSingle(d.email,204.0,y8+1.4,265,10.2,6.8,normal);

      const y9=358.8+shift;
      label('9)','Aadhar No.:',y9);
      fitSingle(d.idNumber,221.0,y9+1.4,245,10.2,7.2,normal);

      const y10=381.8+shift;
      label('10)','Highest Educational Qualification:',y10);
      fitSingle(d.education,351.0,y10+1.4,118,10.2,6.8,normal);

      const y11=403.8+shift;
      label('11)','Occupation:',y11);
      fitSingle(d.occupation,214.4,y11+1.4,255,10.2,7.2,normal);

      const y12=425.8+shift;
      label('12)','Income (Individual):',y12);
      fitSingle(d.individualIncome,270.0,y12+1.4,195,10.2,7.2,normal);

      const catY=452.8+shift;
      label('13)','Category:',catY);
      const c=norm(d.category);
      choice(204,217,catY,'GEN',c==='gen'||c==='general');
      choice(249,262,catY,'EWS',c==='ews');
      choice(298,311,catY,'SC',c==='sc');
      choice(333,346,catY,'ST',c==='st');
      choice(366,379,catY,'OBC',c==='obc');
      choice(411,424,catY,'MINORITY',c==='minority');

      const intY=475.8+shift;
      label('14)','Intention for taking part in the EAP:',intY);
      const intention=norm(d.intention);
      choice(365,382,intY,'a) Employment',intention==='employment');
      choice(365,382,intY+14,'b) Self-employment',intention==='selfemployment');

      const secY=512.8+shift;
      label('13)','In which sector would you like to start business?',secY);
      const sector=norm(d.sector||d.sectors);
      choice(166,204,secY+24,'Fashion technology',sector==='fashiontechnology');
      choice(166,204,secY+38,'Food processing',sector==='foodprocessing');
      choice(166,204,secY+52,'Jute Bag manufacturing',sector==='jutebagmanufacturing');
      choice(166,204,secY+66,'Beautician',sector==='beautician');

      const msdpY=600.8+shift;
      label('14)','Do you want to attend Micro Skill Entrepreneurship Development',msdpY);
      doc.font(normal).fontSize(11.6).text('Programme (MSDP) to understand business?',143.1,msdpY+14,{lineBreak:false});
      const msdp=norm(d.msdpInterest);
      choice(433,451,msdpY+14,'Yes',msdp==='yes');
      choice(505,523,msdpY+14,'No',msdp==='no');

      const decY=643.8+shift;
      doc.font(normal).fontSize(11.4).text(
        'I declare that the above information provided by me is completely correct. I will be responsible if any discrepancy is detected.',
        57.9,decY,{width:482.3,align:'center',lineGap:1}
      );

      const sigBoxY=686.2+shift;
      doc.lineWidth(0.8).rect(408.0,sigBoxY,146.25,43.5).strokeColor('#000').stroke();
      const signature=imageBuffer(d.signatureBase64);
      drawImageFit(signature,412.0,sigBoxY+3,138.25,37.5);

      doc.font(normal).fontSize(11.0).fillColor('#000').text(clean(d.totalSerialNumber),72.0,787.3,{width:150,lineBreak:false});
      doc.text(clean(d.individualSerialNumber),438.0,787.3,{width:90,align:'right',lineBreak:false});

      doc.end();
    } catch(e) {
      reject(e);
    }
  });
}

function makePdfFileName(d) {
  const safe=s=>String(s||'').trim().replace(/\s+/g,'_').replace(/[\\/:*?"<>|#%]/g,'_').replace(/_+/g,'_').slice(0,100);
  const serial=safe(String(d.individualSerialNumber||'').replace(/\//g,'-'));
  return safe(d.name)+'_'+safe(d.place||d.city||'Location')+'_'+(serial||'Pending')+'.pdf';
}
