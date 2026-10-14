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

app.get('/downloads/HAL_EAP_Registration_Android_v2_8.apk',(req,res)=>{
  res.redirect(302,'/downloads/HAL_EAP_Registration_Android_latest.apk');
});

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
      const doc = new PDFDocument({size:[595,842],margin:0,compress:true});
      const chunks=[];
      doc.on('data',c=>chunks.push(c));
      doc.on('end',()=>resolve(Buffer.concat(chunks)));
      doc.on('error',reject);

      const regular='Times-Roman';
      const bold='Times-Bold';
      const clean=v=>String(v==null?'':v).trim();
      const relationLabel=()=>{
        const r=clean(d.guardianRelation).toLowerCase();
        if(r==='father') return "Father's Name:";
        if(r==='husband') return "Husband's Name:";
        if(r==='mother') return "Mother's Name:";
        return "Father's/Husband's/Mother's Name:";
      };
      const font=(name,size)=>{doc.font(name).fontSize(size);return doc;};
      const topForBaseline=(name,size,baseline)=>{
        font(name,size);
        const asc=(doc._font && Number.isFinite(doc._font.ascender))?doc._font.ascender:750;
        return baseline-(size*asc/1000);
      };
      const draw=(text,x,baseline,size,name=regular,opts={})=>{
        const value=clean(text);
        font(name,size).fillColor('#000');
        doc.text(value,x,topForBaseline(name,size,baseline),{lineBreak:false,...opts});
      };
      const centered=(text,cx,baseline,size,name=regular)=>{
        const value=clean(text);
        font(name,size);
        const x=cx-doc.widthOfString(value)/2;
        draw(value,x,baseline,size,name);
      };
      const fit=(text,x,baseline,maxWidth,startSize=10.2,minSize=7,name=regular)=>{
        let value=clean(text),size=startSize;
        font(name,size);
        while(value && doc.widthOfString(value)>maxWidth && size>minSize){
          size-=0.25;font(name,size);
        }
        if(value && doc.widthOfString(value)>maxWidth){
          let fitted=value;
          while(fitted.length>3 && doc.widthOfString(fitted+'...')>maxWidth) fitted=fitted.slice(0,-1);
          value=fitted+'...';
        }
        draw(value,x,baseline,size,name);
      };
      const wrapLines=(text,maxWidth,size,name=regular)=>{
        const value=clean(text);
        if(!value)return [''];
        font(name,size);
        const words=value.split(/\s+/);
        const lines=[];
        let current='';
        for(const word of words){
          const candidate=current?current+' '+word:word;
          if(doc.widthOfString(candidate)<=maxWidth){
            current=candidate;
          }else{
            if(current){lines.push(current);current='';}
            let remaining=word;
            while(remaining && doc.widthOfString(remaining)>maxWidth){
              let take=remaining.length;
              while(take>1 && doc.widthOfString(remaining.slice(0,take))>maxWidth)take--;
              lines.push(remaining.slice(0,take));
              remaining=remaining.slice(take);
            }
            current=remaining;
          }
        }
        if(current)lines.push(current);
        return lines.length?lines:[value];
      };
      const wrapped=(text,x,baseline,maxWidth,startSize=9.4,minSize=6.8,maxLines=3,lineHeight=10,name=regular)=>{
        let size=startSize,lines=wrapLines(text,maxWidth,size,name);
        while(lines.length>maxLines && size>minSize){
          size-=0.25;
          lines=wrapLines(text,maxWidth,size,name);
        }
        if(lines.length>maxLines){
          const kept=lines.slice(0,maxLines);
          kept[maxLines-1]=lines.slice(maxLines-1).join(' ');
          lines=kept;
          while(doc.widthOfString(lines[maxLines-1])>maxWidth && size>minSize){
            size-=0.25;font(name,size);
          }
        }
        font(name,size);
        const count=Math.min(maxLines,Math.max(1,lines.length));
        for(let i=0;i<count;i++)draw(lines[i],x,baseline+(i*lineHeight),size,name);
        return count;
      };
      const box=(x,y,size,checked)=>{
        doc.lineWidth(1).strokeColor('#000').rect(x,y,size,size).stroke();
        if(checked){
          doc.lineWidth(1.6).moveTo(x+2,y+size*.55).lineTo(x+size*.42,y+size-2).lineTo(x+size-2,y+2).stroke();
        }
      };
      const checkOption=(boxX,boxY,text,textX,textY,checked)=>{
        box(boxX,boxY,10,checked); draw(text,textX,textY,12,regular);
      };
      const imageBuffer=v=>{
        const raw=clean(v).replace(/^data:[^;]+;base64,/,'');
        return raw?Buffer.from(raw,'base64'):null;
      };
      const drawLogo=(buf,x,y,w,h)=>{try{doc.image(buf,x,y,{fit:[w,h],align:'center',valign:'center'});}catch(_){}};
      const drawPhoto=(buf)=>{
        const x=492.75,y=178.43,w=75.45,h=101.25;
        doc.lineWidth(1).rect(x,y,w,h).strokeColor('#000').stroke();
        if(buf){
          try{
            doc.save().rect(x+1,y+1,w-2,h-2).clip();
            doc.image(buf,x,y,{cover:[w,h],align:'center',valign:'center'});
            doc.restore();
            return;
          }catch(_){try{doc.restore();}catch(__){}}
        }
        draw('PHOTO',510.48,229,11.04,bold);
      };
      const drawSignature=(buf)=>{
        const x=408,y=686.15,w=146.25,h=43.5;
        doc.lineWidth(1).rect(x,y,w,h).strokeColor('#000').stroke();
        if(buf){
          try{doc.image(buf,x+4,y+4,{fit:[w-8,h-8],align:'center',valign:'center'});return;}catch(_){}
        }
        draw('SIGNATURE',454,713,11.04,bold);
      };

      // Exact Android registration form coordinates.
      drawLogo(HAL_FORM_LOGO,21.75,7.5,94.5,50.5);
      drawLogo(EDII_FORM_LOGO,483.1,6.75,104.94,48.8);

      centered('Entrepreneurship Awareness Programme (EAP)',300,20.5,14.04,bold);
      centered('Organized by',300,36.5,12,regular);
      centered('Hindustan Aeronautics Limited (HAL)',300,51.5,12,bold);
      centered('In Collaboration with',300,66.5,12,regular);
      centered('Entrepreneurship Development Institute of India (EDII)',300,81.5,12,bold);
      centered('Website: www.ediindia.org, www. ediindia.ac.in',300,95.5,11.04,regular);
      centered('*'.repeat(88),300,117,11.04,regular);
      centered('Registration Form',302,132,12,bold);

      draw('Date:',83.06,148,12,regular);
      fit(d.eapDate,118,148,180,10.2,8,regular);
      draw('Place:',410.02,148,12,regular);
      fit(d.place||d.city,449,148,118,10.2,8,regular);
      centered('*'.repeat(88),300,161,11.04,regular);

      drawPhoto(imageBuffer(d.photoBase64));

      draw('1)',89.3,189,12,regular); draw('Full Name:',143.3,189,12,regular); fit(d.name,216,189,265,10.2,7.5,regular);
      draw('2)',89.3,215,12,regular); draw('Gender:',143.3,215,12,regular); fit(d.gender,195,215,285,10.2,8,regular);
      draw('3)',89.3,237,12,regular); draw('Date of Birth:',143.3,237,12,regular); fit(d.dob,229,237,250,10.2,8,regular);
      const guardianText=relationLabel();
      draw('4)',89.3,259,12,regular);
      draw(guardianText,143.3,259,12,regular);
      font(regular,12);
      const guardianValueX=Math.min(350,143.3+doc.widthOfString(guardianText)+8);
      fit(d.guardianName,guardianValueX,259,480-guardianValueX,10.2,7,regular);

      draw('5)',89.3,281,12,regular);
      draw('Full Address:',143.3,281,12,regular);
      const addressLines=wrapped(d.fullAddress||'',228,281,252,9.4,6.8,3,10,regular);
      const addressShift=Math.max(0,addressLines-1)*9;

      const y6=303+addressShift,y7=325+addressShift,y8=347+addressShift,y9=369+addressShift;
      const y10=392+addressShift,y11=414+addressShift,y12=436+addressShift,y13=463+addressShift;

      draw('6)',89.3,y6,12,regular); draw('Mobile Phone:',143.3,y6,12,regular); fit(d.mobile,232,y6,247,10.2,8,regular);
      draw('7)',89.3,y7,12,regular); draw('Alternative Phone No.:',143.3,y7,12,regular); fit(d.alternateMobile,280,y7,200,10.2,8,regular);
      draw('8)',89.3,y8,12,regular); draw('Email ID:',143.3,y8,12,regular); fit(d.email,204,y8,276,10.2,7.5,regular);
      draw('9)',89.3,y9,12,regular); draw('Aadhar No.:',143.3,y9,12,regular); fit(d.idNumber,219,y9,260,10.2,8,regular);
      draw('10)',89.3,y10,12,regular); draw('Highest Educational Qualification:',143.3,y10,12,regular); fit(d.education,351,y10,128,10.2,6.8,regular);
      draw('11)',89.3,y11,12,regular); draw('Occupation:',143.3,y11,12,regular); fit(d.occupation,219,y11,260,10.2,8,regular);
      draw('12)',89.3,y12,12,regular); draw('Income (Individual):',143.3,y12,12,regular); fit(d.individualIncome,263,y12,216,10.2,8,regular);

      draw('13)',89.3,y13,12,regular); draw('Category:',143.3,y13,12,regular);
      const category=clean(d.category).toUpperCase();
      checkOption(202.61,449.6+addressShift,'GEN',216.05,y13,category==='GEN');
      checkOption(247.01,449.6+addressShift,'EWS',261.89,y13,category==='EWS');
      checkOption(293.83,449.6+addressShift,'SC',308.59,y13,category==='SC');
      checkOption(329.23,449.6+addressShift,'ST',343.99,y13,category==='ST');
      checkOption(363.19,449.6+addressShift,'OBC',378.10,y13,category==='OBC');
      checkOption(409.30,449.6+addressShift,'MINORITY',424.06,y13,category==='MINORITY');

      const intentionY=486+addressShift;
      draw('14)',89.3,intentionY,12,regular); draw('Intention for taking part in the EAP:',143.3,intentionY,12,regular);
      const intention=clean(d.intention).toLowerCase().replace(/[^a-z]/g,'');
      box(364.5,477.2+addressShift,12,intention==='employment'); draw('a) Employment',382.18,intentionY,12,regular);
      box(363.75,490.73+addressShift,12,intention==='selfemployment'); draw('b) Self-employment',383.14,500+addressShift,12,regular);

      const sectorY=523+addressShift; draw('13)',86.18,sectorY,12,regular); draw('In which sector would you like to start business?',143.06,sectorY,12,regular);
      const sector=clean(d.sector||d.sectors).toLowerCase().replace(/[^a-z]/g,'');
      box(166.3,537,12,sector==='fashiontechnology'); draw('Fashion technology',204.77,547,12,regular);
      box(166.3,551.05,12,sector==='foodprocessing'); draw('Food processing',204.77,561,12,regular);
      box(166.3,565.2,12,sector==='jutebagmanufacturing'); draw('Jute Bag manufacturing',204.77,575,12,regular);
      box(166.3,579.25,12,sector==='beautician'); draw('Beautician',204.77,589,12,regular);

      const msdpY=611+addressShift; draw('14)',86.18,msdpY,12,regular);
      draw('Do you want to attend Micro Skill Entrepreneurship Development',143.06,msdpY,12,regular);
      draw('Programme (MSDP) to understand business?',142.94,625+addressShift,12,regular);
      const msdp=clean(d.msdpInterest).toLowerCase();
      box(433.28,615.83+addressShift,12,msdp==='yes'); draw('Yes',451.06,625+addressShift,12,regular);
      box(505.73,615.83+addressShift,12,msdp==='no'); draw('No',523.44,625+addressShift,12,regular);

      draw('I declare that the above information provided by me is completely correct. I will be',57.86,654+addressShift,12,regular);
      draw('responsible if any discrepancy is detected.',114.62,669+addressShift,12,regular);

      const signatureShift=addressShift;
      const originalDrawSignature=drawSignature;
      // Keep the signature box aligned below the declaration when address wraps.
      if(signatureShift===0){
        originalDrawSignature(imageBuffer(d.signatureBase64));
      }else{
        const signature=imageBuffer(d.signatureBase64);
        const x=408,y=686.15+signatureShift,w=146.25,h=43.5;
        doc.lineWidth(1).rect(x,y,w,h).strokeColor('#000').stroke();
        if(signature){try{doc.image(signature,x+4,y+4,{fit:[w-8,h-8],align:'center',valign:'center'});}catch(_){}}
      }
      fit(d.totalSerialNumber||'EAP/2026/N',72.02,797,180,11.04,8,regular);
      fit(d.individualSerialNumber||'EAP/2026/K-S/N',438.46,797,145,11.04,8,regular);
      centered('Designed and Developed by Ajay Rao, EDII SRO Bengaluru.',300,827,8.2,regular);

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
