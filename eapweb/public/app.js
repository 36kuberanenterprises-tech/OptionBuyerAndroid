const $=id=>document.getElementById(id);
const CODES={
  'Kolar | Mulbagal':'K-M','Kolar | Srinivaspur':'K-S',
  'Bengaluru Rural | Devanahalli':'BR-D','Bengaluru Rural | Hoskote':'BR-H',
  'Tumkur | Tumkur':'T-T','Tumkur | Gubbi':'T-G','Tumkur | Sira':'T-S',
  'Bengaluru South | Ramanagara':'BS-R','Bengaluru South | Channapatna':'BS-C'
};
let photoBase64='',signatureBase64='',installPrompt=null,dbPromise=null,currentPdfRecord=null,currentPdfObjectUrl='';
document.addEventListener('DOMContentLoaded',init);

async function init(){
  $('eapDate').value=localISODate(new Date());
  $('projectLocation').addEventListener('change',()=>{ $('city').value=placeOf($('projectLocation').value); refreshStatus(); });
  $('dob').addEventListener('change',updateAge); $('eapDate').addEventListener('change',updateAge);
  bindImageInput('photoCamera','photo');
  bindImageInput('photoUpload','photo');
  bindImageInput('signatureCamera','signature');
  bindImageInput('signatureUpload','signature');
  $('registrationForm').addEventListener('submit',submit);
  $('syncBtn').addEventListener('click',syncQueue); $('syncTop').addEventListener('click',syncQueue);
  $('nextBtn').addEventListener('click',()=>{ $('resultDialog').close(); clearNext(); });
  $('closeBtn').addEventListener('click',()=>$('resultDialog').close());
  $('viewResultPdfBtn').addEventListener('click',()=>viewRecordPdf(currentPdfRecord));
  $('downloadResultPdfBtn').addEventListener('click',()=>downloadRecordPdf(currentPdfRecord));
  $('openCentralPdfBtn').addEventListener('click',()=>{if(currentPdfRecord&&currentPdfRecord.pdfUrl)window.open(currentPdfRecord.pdfUrl,'_blank','noopener')});
  window.addEventListener('online',()=>{updateNetwork();syncQueue();}); window.addEventListener('offline',updateNetwork);
  window.addEventListener('beforeinstallprompt',e=>{e.preventDefault();installPrompt=e;$('installBtn').hidden=false;});
  $('installBtn').addEventListener('click',installApp);
  if('serviceWorker'in navigator) navigator.serviceWorker.register('/sw.js').catch(()=>{});
  updateNetwork(); await renderApps(); await pendingCount(); refreshStatus();
  setTimeout(()=>$('splash').classList.add('hide'),1500);
}
function updateAge(){const a=$('dob').value,b=$('eapDate').value;if(!a||!b){$('age').value='';return}const d=new Date(a+'T00:00:00'),e=new Date(b+'T00:00:00');if(d>e){$('age').value='';return}let y=e.getFullYear()-d.getFullYear();const m=e.getMonth()-d.getMonth();if(m<0||(m===0&&e.getDate()<d.getDate()))y--;$('age').value=Math.max(0,y)}
function bindImageInput(id,type){
  const el=$(id);
  if(!el)return;
  el.addEventListener('click',()=>{ el.value=''; });
  el.addEventListener('change',e=>loadImage(e,type));
}

async function loadImage(ev,type){
  const input=ev.target;
  const file=input.files&&input.files[0];
  if(!file)return;

  const statusEl=$(type==='photo'?'photoStatus':'signatureStatus');
  if(statusEl){
    statusEl.textContent='Processing image…';
    statusEl.className='uploadstatus processing';
  }

  try{
    if(!file.type.startsWith('image/')){
      throw new Error('Please select an image file.');
    }

    // Keep browser memory and Apps Script payload under control.
    const maxBytes=12*1024*1024;
    if(file.size>maxBytes){
      throw new Error('Image is too large. Please choose a photo below 12 MB.');
    }

    const base64=await compressImage(
      file,
      type==='signature'?1200:1600,
      type==='signature'?0.82:0.80
    );

    const preview=$(type==='photo'?'photoPreview':'signaturePreview');
    if(type==='photo') photoBase64=base64;
    else signatureBase64=base64;

    if(preview){
      preview.src='data:image/jpeg;base64,'+base64;
      preview.style.display='block';
    }

    if(statusEl){
      statusEl.textContent=type==='photo'?'Photo ready ✓':'Signature ready ✓';
      statusEl.className='uploadstatus ready';
    }
  }catch(err){
    if(type==='photo') photoBase64='';
    else signatureBase64='';

    if(statusEl){
      statusEl.textContent=err&&err.message?err.message:'Unable to read selected image.';
      statusEl.className='uploadstatus error';
    }
    status(err&&err.message?err.message:'Unable to read selected image.',true);
  }
}

async function compressImage(file,maxDimension,quality){
  let bitmap=null;
  let objectUrl='';

  try{
    // createImageBitmap works well on modern Chrome/Edge/Android.
    if('createImageBitmap' in window){
      try{ bitmap=await createImageBitmap(file,{imageOrientation:'from-image'}); }catch(_){}
    }

    if(bitmap){
      const scale=Math.min(1,maxDimension/Math.max(bitmap.width,bitmap.height));
      const w=Math.max(1,Math.round(bitmap.width*scale));
      const h=Math.max(1,Math.round(bitmap.height*scale));
      const canvas=document.createElement('canvas');
      canvas.width=w; canvas.height=h;
      const ctx=canvas.getContext('2d',{alpha:false});
      ctx.fillStyle='#fff'; ctx.fillRect(0,0,w,h);
      ctx.drawImage(bitmap,0,0,w,h);
      if(bitmap.close)bitmap.close();
      const data=canvas.toDataURL('image/jpeg',quality);
      if(!data||!data.includes(','))throw new Error('Could not process image.');
      return data.split(',')[1];
    }

    // Fallback for Safari/iPhone and older Android browsers.
    objectUrl=URL.createObjectURL(file);
    const img=await new Promise((resolve,reject)=>{
      const im=new Image();
      im.onload=()=>resolve(im);
      im.onerror=()=>reject(new Error('This image format could not be opened. Please use JPG/PNG or take a new photo.'));
      im.src=objectUrl;
    });

    const scale=Math.min(1,maxDimension/Math.max(img.naturalWidth||img.width,img.naturalHeight||img.height));
    const w=Math.max(1,Math.round((img.naturalWidth||img.width)*scale));
    const h=Math.max(1,Math.round((img.naturalHeight||img.height)*scale));
    const canvas=document.createElement('canvas');
    canvas.width=w; canvas.height=h;
    const ctx=canvas.getContext('2d',{alpha:false});
    ctx.fillStyle='#fff'; ctx.fillRect(0,0,w,h);
    ctx.drawImage(img,0,0,w,h);
    const data=canvas.toDataURL('image/jpeg',quality);
    if(!data||!data.includes(','))throw new Error('Could not process image.');
    return data.split(',')[1];
  }finally{
    if(objectUrl)URL.revokeObjectURL(objectUrl);
  }
}

function validate(){const req=[['eapDate','EAP Date'],['projectLocation','HAL Project Location'],['name','Name'],['dob','Date of Birth'],['guardianRelation','Relation'],['guardianName','Father / Husband / Mother Name'],['address','Address'],['panchayat','Panchayat'],['pinCode','PIN Code'],['mobile','Mobile Number'],['education','Educational Qualification'],['occupation','Occupation'],['category','Category']];for(const [i,l] of req)if(!$(i).value.trim())return l+' is required.';for(const g of ['gender','intention','sector','msdp'])if(!checked(g))return 'Please select '+g+'.';if(!/^\d{6}$/.test($('pinCode').value))return'PIN Code must contain 6 digits.';if(!/^[6-9]\d{9}$/.test($('mobile').value))return'Enter a valid 10 digit mobile number.';if($('alternateMobile').value&&!/^[6-9]\d{9}$/.test($('alternateMobile').value))return'Enter a valid alternate mobile number.';if($('aadhaar').value&&!/^\d{12}$/.test($('aadhaar').value))return'Aadhaar must contain exactly 12 digits.';if(!photoBase64)return'Candidate photo is required.';if(!signatureBase64)return'Applicant signature photo is required.';if(!$('declaration').checked)return'Please accept the declaration.';return''}
function payload(){const p=$('projectLocation').value,city=placeOf(p),id=crypto.randomUUID?crypto.randomUUID():Date.now()+'-'+Math.random();return{clientRequestId:id,projectLocation:p,locationCode:CODES[p]||'NA',place:city,eapDate:displayDate($('eapDate').value),name:$('name').value.trim(),gender:checked('gender'),dob:displayDate($('dob').value),ageOnEapDate:$('age').value,guardianRelation:$('guardianRelation').value,guardianName:$('guardianName').value.trim(),village:$('address').value.trim(),panchayat:$('panchayat').value.trim(),block:'',city,pinCode:$('pinCode').value.trim(),state:'Karnataka',fullAddress:[$('address').value.trim(),$('panchayat').value.trim(),projectAddress(p),'Karnataka','PIN '+$('pinCode').value.trim()].filter(Boolean).join(', '),mobile:$('mobile').value.trim(),alternateMobile:$('alternateMobile').value.trim(),email:$('email').value.trim(),idType:'Aadhaar',idNumber:$('aadhaar').value.trim(),education:$('education').value.trim(),occupation:$('occupation').value.trim(),individualIncome:$('income').value.trim(),category:$('category').value,intention:checked('intention'),sector:checked('sector'),sectors:checked('sector'),msdpInterest:checked('msdp'),photoBase64,signatureType:'Photo',signatureBase64,declarationAccepted:true}}
async function submit(e){
  e.preventDefault();
  const er=validate();
  if(er)return status(er,true);
  const p=payload();
  $('submitBtn').disabled=true;
  $('submitBtn').textContent=navigator.onLine?'Submitting…':'Saving Offline…';
  if(!navigator.onLine){
    await save({...p,status:'PENDING',createdAt:Date.now(),lastError:'Waiting for internet'});
    await queued(p.name);
    return;
  }
  try{
    const r=await api(p);
    const syncedRecord={...p,status:'SYNCED',createdAt:Date.now(),syncedAt:Date.now(),totalSerialNumber:r.totalSerialNumber||'',individualSerialNumber:r.individualSerialNumber||'',pdfUrl:r.pdfUrl||'',editableUrl:r.editableUrl||'',folderUrl:r.folderUrl||'',pdfFileName:r.pdfFileName||'',applicationPdfBase64:r.applicationPdfBase64||''};
    await save(syncedRecord);
    showResult(p.name,r,false,syncedRecord);
    status('Application submitted and synchronized. The filled application is ready to view or download.',false);
    await renderApps();
    await pendingCount();
    refreshStatus();
  }catch(ex){
    await save({...p,status:'PENDING',createdAt:Date.now(),lastError:ex.message||'Sync unavailable'});
    await queued(p.name);
  }finally{
    $('submitBtn').disabled=false;
    $('submitBtn').textContent='Submit Application';
  }
}
async function api(p){const r=await fetch('/api/submit',{method:'POST',headers:{'Content-Type':'application/json','X-EAP-Token':getToken()},body:JSON.stringify(p)});const d=await r.json().catch(()=>({}));if(!r.ok||!d.success)throw new Error(d.message||'Server error '+r.status);return d}
async function refreshStatus(){if(!navigator.onLine){$('totalNext').textContent='Offline • updates when connected';$('locationNext').textContent=$('projectLocation').value?'Serial assigned when internet returns':'Select HAL Project Location';return}const p=$('projectLocation').value;try{const r=await fetch('/api/status',{method:'POST',headers:{'Content-Type':'application/json','X-EAP-Token':getToken()},body:JSON.stringify({projectLocation:p,locationCode:CODES[p]||'NA'})});const d=await r.json();if(!d.success)return;$('totalCount').textContent=d.totalApplicationCount??'—';$('totalNext').textContent=d.nextTotalSerial?'Next: '+d.nextTotalSerial:'Central serial control';if(p){$('locationCount').textContent=d.locationApplicationCount??'—';$('locationNext').textContent=d.nextLocationSerial?'Next: '+d.nextLocationSerial:'Central serial control';$('locationName').textContent=placeOf(p)}else{$('locationCount').textContent='—';$('locationNext').textContent='Select HAL Project Location';$('locationName').textContent='Central serial control'}}catch(_){}}
async function queued(name){const p=await pend();status('Saved safely on this device. Pending sync: '+p.length,false);showResult(name,{pendingCount:p.length},true);await renderApps();await pendingCount();$('submitBtn').disabled=false;$('submitBtn').textContent='Submit Application'}
async function syncQueue(){
  if(!navigator.onLine)return status('No internet. Pending applications remain safely stored on this device.',true);
  const p=await pend();
  if(!p.length){status('No pending applications to sync.',false);refreshStatus();return}
  status('Synchronizing '+p.length+' pending application(s)…',false);
  let ok=0;
  for(const r of p){
    try{
      const d=await api(strip(r));
      await save({...r,status:'SYNCED',syncedAt:Date.now(),lastError:'',totalSerialNumber:d.totalSerialNumber||'',individualSerialNumber:d.individualSerialNumber||'',pdfUrl:d.pdfUrl||'',editableUrl:d.editableUrl||'',folderUrl:d.folderUrl||'',pdfFileName:d.pdfFileName||'',applicationPdfBase64:d.applicationPdfBase64||''});
      ok++;
    }catch(e){
      await save({...r,status:'PENDING',lastError:e.message||'Sync failed'});
    }
  }
  await renderApps();
  await pendingCount();
  refreshStatus();
  const left=(await pend()).length;
  status('Synced '+ok+'. Pending '+left+'.',left>0);
}
function showResult(n,r,q,record=null){
  $('dialogTitle').textContent=q?'Saved for Offline Sync':'Application Submitted';
  $('dialogText').textContent=q?n+' has been saved safely on this device.':n+' has been synchronized to EAP_Master Sheet. The filled application is shown below.';
  $('dialogSerials').textContent=q?'Pending applications on this device: '+r.pendingCount+'\nCentral serial and filled application PDF will be created automatically when internet returns.':'Application Serial: '+(r.totalSerialNumber||'')+'\nLocation Serial: '+(r.individualSerialNumber||'')+'\nPDF: '+(r.pdfFileName||'Generated');
  currentPdfRecord=q?null:{...(record||{}),name:n,totalSerialNumber:r.totalSerialNumber||(record&&record.totalSerialNumber)||'',individualSerialNumber:r.individualSerialNumber||(record&&record.individualSerialNumber)||'',pdfFileName:r.pdfFileName||(record&&record.pdfFileName)||'EAP_Application.pdf',pdfUrl:r.pdfUrl||(record&&record.pdfUrl)||'',applicationPdfBase64:r.applicationPdfBase64||(record&&record.applicationPdfBase64)||''};
  $('pdfResultArea').hidden=q;
  $('openCentralPdfBtn').hidden=q||!(currentPdfRecord&&currentPdfRecord.pdfUrl);
  if(!q)prepareResultPreview(currentPdfRecord);
  $('resultDialog').showModal();
}

function pdfBlobFromBase64(base64){
  const clean=String(base64||'').replace(/^data:application\/pdf;base64,/,'');
  const binary=atob(clean);
  const bytes=new Uint8Array(binary.length);
  for(let i=0;i<binary.length;i++)bytes[i]=binary.charCodeAt(i);
  return new Blob([bytes],{type:'application/pdf'});
}

function releasePdfObjectUrl(){
  if(currentPdfObjectUrl){
    URL.revokeObjectURL(currentPdfObjectUrl);
    currentPdfObjectUrl='';
  }
}

async function pdfBlobForRecord(record){
  if(!record)throw new Error('Application PDF is not available yet.');
  if(record.applicationPdfBase64)return pdfBlobFromBase64(record.applicationPdfBase64);
  const response=await fetch('/api/pdf',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(strip(record))});
  if(!response.ok){
    const d=await response.json().catch(()=>({}));
    throw new Error(d.message||'Unable to generate application PDF.');
  }
  return await response.blob();
}

async function prepareResultPreview(record){
  const frame=$('resultPdfPreview'),note=$('pdfPreviewNote');
  releasePdfObjectUrl();
  frame.removeAttribute('src');
  note.textContent='Preparing the filled application preview…';
  try{
    const blob=await pdfBlobForRecord(record);
    currentPdfObjectUrl=URL.createObjectURL(blob);
    frame.src=currentPdfObjectUrl+'#toolbar=1&navpanes=0&view=FitH';
    note.textContent='Filled application ready. You can view it here or download a copy to this system.';
  }catch(e){
    note.textContent=(e.message||'Preview could not be loaded.')+' Use View Filled Application or Download PDF to try again.';
  }
}

async function viewRecordPdf(record){
  if(!record)return status('Application PDF is not available yet.',true);
  const popup=window.open('about:blank','_blank');
  try{
    const blob=await pdfBlobForRecord(record);
    const url=URL.createObjectURL(blob);
    if(popup)popup.location.href=url;
    else status('The browser blocked a new tab. The filled application is already shown in the preview.',true);
    setTimeout(()=>URL.revokeObjectURL(url),120000);
  }catch(e){
    if(popup)popup.close();
    if(record.pdfUrl)window.open(record.pdfUrl,'_blank','noopener');
    else status(e.message||'Unable to open application PDF.',true);
  }
}

async function downloadRecordPdf(record){
  if(!record)return status('Application PDF is not available yet.',true);
  try{
    const blob=await pdfBlobForRecord(record);
    const url=URL.createObjectURL(blob);
    const a=document.createElement('a');
    a.href=url;
    a.download=record.pdfFileName||safePdfName(record);
    a.style.display='none';
    document.body.appendChild(a);
    a.click();
    setTimeout(()=>{a.remove();URL.revokeObjectURL(url)},30000);
    status('Application PDF download started.',false);
  }catch(e){
    if(record.pdfUrl)window.open(record.pdfUrl,'_blank','noopener');
    else status(e.message||'Unable to download application PDF.',true);
  }
}

function safePdfName(record){
  const name=String((record&&record.name)||'Applicant').replace(/[^a-zA-Z0-9._ ]+/g,'_').replace(/\s+/g,'_').slice(0,80);
  const serial=String((record&&record.individualSerialNumber)||'').replace(/\//g,'-').replace(/[^a-zA-Z0-9._-]+/g,'_');
  return name+'_'+(serial||'EAP_Application')+'.pdf';
}

function clearNext(){
  releasePdfObjectUrl();
  currentPdfRecord=null;
  $('pdfResultArea').hidden=true;
  $('resultPdfPreview').removeAttribute('src');
  const d=$('eapDate').value,l=$('projectLocation').value;
  $('registrationForm').reset();
  $('eapDate').value=d;
  $('projectLocation').value=l;
  $('state').value='Karnataka';
  $('city').value=placeOf(l);
  photoBase64='';
  signatureBase64='';
  $('photoPreview').removeAttribute('src');
  $('signaturePreview').removeAttribute('src');
  $('age').value='';
  status('',false);
  scrollTo({top:0,behavior:'smooth'});
  refreshStatus();
}
function updateNetwork(){$('networkText').textContent=navigator.onLine?'Online • central synchronization available':'Offline • applications will be queued on this device';$('networkText').style.color=navigator.onLine?'#1c7d27':'#9a6700'}
function status(m,e){$('statusMessage').textContent=m;$('statusMessage').style.color=e?'#b42318':'#1c7d27'}function checked(n){const e=document.querySelector('input[name="'+n+'"]:checked');return e?e.value:''}function placeOf(l){if(!l)return'';const i=l.indexOf('|');return i>=0?l.slice(i+1).trim():l.trim()}function projectAddress(l){if(!l)return'';const i=l.indexOf('|');if(i<0)return l;const d=l.slice(0,i).trim(),p=l.slice(i+1).trim();return d===p?p:p+', '+d}function displayDate(i){if(!i)return'';const[y,m,d]=i.split('-');return d+'/'+m+'/'+y}function localISODate(d){const z=n=>String(n).padStart(2,'0');return d.getFullYear()+'-'+z(d.getMonth()+1)+'-'+z(d.getDate())}function strip(r){const c={...r};for(const k of['status','createdAt','syncedAt','lastError','pdfUrl','editableUrl','folderUrl','pdfFileName','applicationPdfBase64'])delete c[k];return c}
function openDb(){if(dbPromise)return dbPromise;dbPromise=new Promise((ok,bad)=>{const r=indexedDB.open('hal_eap_web',1);r.onupgradeneeded=()=>{const db=r.result;if(!db.objectStoreNames.contains('registrations'))db.createObjectStore('registrations',{keyPath:'clientRequestId'})};r.onsuccess=()=>ok(r.result);r.onerror=()=>bad(r.error)});return dbPromise}
async function save(r){const db=await openDb();return new Promise((ok,bad)=>{const t=db.transaction('registrations','readwrite');t.objectStore('registrations').put(r);t.oncomplete=ok;t.onerror=()=>bad(t.error)})}async function all(){const db=await openDb();return new Promise((ok,bad)=>{const r=db.transaction('registrations','readonly').objectStore('registrations').getAll();r.onsuccess=()=>ok(r.result||[]);r.onerror=()=>bad(r.error)})}async function pend(){return(await all()).filter(r=>r.status!=='SYNCED').sort((a,b)=>(a.createdAt||0)-(b.createdAt||0))}async function pendingCount(){const n=(await pend()).length;$('pendingCount').textContent='Pending on this device: '+n}async function renderApps(){
  const rows=(await all()).sort((a,b)=>(b.createdAt||0)-(a.createdAt||0)),h=$('applicationsList');
  h.innerHTML='';
  if(!rows.length){
    h.innerHTML='<p class="muted">No applications saved on this device yet.</p>';
    return;
  }
  rows.slice(0,100).forEach(r=>{
    const d=document.createElement('div');
    d.className='approw';
    d.innerHTML='<b>'+esc(r.name||'Applicant')+'</b><br><small>'+esc(r.totalSerialNumber||'Pending central serial')+' • '+esc(r.projectLocation||'')+'</small><br><span class="badge '+(r.status==='SYNCED'?'synced':'pending')+'">'+(r.status==='SYNCED'?'Synchronized':'Pending Sync')+'</span>'+(r.lastError?' <small>'+esc(r.lastError)+'</small>':'');
    if(r.status==='SYNCED'){
      const actions=document.createElement('div');
      actions.className='appactions';
      const view=document.createElement('button');
      view.type='button';
      view.className='appaction secondary';
      view.textContent='View Filled Application';
      view.addEventListener('click',()=>viewRecordPdf(r));
      actions.appendChild(view);
      const download=document.createElement('button');
      download.type='button';
      download.className='appaction';
      download.textContent='Download PDF';
      download.addEventListener('click',()=>downloadRecordPdf(r));
      actions.appendChild(download);
      if(r.pdfUrl){
        const central=document.createElement('a');
        central.className='appaction central';
        central.href=r.pdfUrl;
        central.target='_blank';
        central.rel='noopener';
        central.textContent='Open Central Copy';
        actions.appendChild(central);
      }
      d.appendChild(actions);
    }
    h.appendChild(d);
  });
}
function esc(s){return String(s||'').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m]))}
async function installApp(){window.location.href='/install.html'}

function getToken(){return localStorage.getItem('eap_api_token')||''}
function ensureToken(){}
