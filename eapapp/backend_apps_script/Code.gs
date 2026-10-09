const MASTER_SHEET_ID = '17d_uGX6VHqes6AY8ncjsA_-jOco-397csqDV5dFahKs';
const MASTER_SHEET_NAME = 'Master Applications';
const RESERVATION_SHEET_NAME = 'Serial Reservations';
const CENTRAL_ROOT_FOLDER_NAME = 'HAL EAP Central Records';
const YEAR = '2026';

function doPost(e) {
  try {
    const payload = JSON.parse((e && e.parameter && e.parameter.payload) || '{}');
    const props = PropertiesService.getScriptProperties();
    const expectedToken = String(props.getProperty('API_TOKEN') || '').trim();

    if (!expectedToken || String(payload.token || '').trim() !== expectedToken) {
      return jsonResponse(false, 'Unauthorised request.');
    }

    const action = String(payload.action || 'submitRegistration');
    const ss = SpreadsheetApp.openById(MASTER_SHEET_ID);
    const master = getOrCreateMasterSheet(ss);
    const reservations = getOrCreateReservationSheet(ss);

    if (action === 'getStatus') {
      return getCentralStatus(master, reservations, payload);
    }

    if (action === 'reserveSerial') {
      return reserveSerial(master, reservations, payload);
    }

    return submitRegistration(master, reservations, payload);

  } catch (err) {
    return jsonResponse(false, 'Error: ' + err.message);
  }
}

function getCentralStatus(master, reservations, payload) {
  const projectLocation = String(payload.projectLocation || '').trim();
  const locationCode = String(payload.locationCode || '').trim() || locationCodeFor(projectLocation);

  const serials = calculateNextSerials(master, reservations, locationCode);
  const counts = submittedCounts(master, projectLocation);

  return jsonResponse(true, 'Central status loaded.', {
    targetSheet: 'EAP_Master Sheet',
    masterSheetName: MASTER_SHEET_NAME,
    totalApplicationCount: counts.total,
    locationApplicationCount: counts.location,
    nextTotalSerial: serials.totalSerial,
    nextLocationSerial: serials.locationSerial,
    centralOnly: true
  });
}

function reserveSerial(master, reservations, payload) {
  const requestId = String(payload.clientRequestId || '').trim();
  const projectLocation = String(payload.projectLocation || '').trim();
  const locationCode = String(payload.locationCode || '').trim() || locationCodeFor(projectLocation);
  const applicantName = String(payload.name || '').trim();

  if (!requestId) return jsonResponse(false, 'Client Request ID is missing.');
  if (!projectLocation || !locationCode || locationCode === 'NA') {
    return jsonResponse(false, 'Valid HAL Project Location is required.');
  }

  const lock = LockService.getScriptLock();
  lock.waitLock(30000);
  try {
    const existingMaster = findMasterByRequestId(master, requestId);
    if (existingMaster) {
      return jsonResponse(true, 'Already submitted.', existingMaster);
    }

    const existingReservation = findReservation(reservations, requestId);
    if (existingReservation) {
      return jsonResponse(true, 'Serial already reserved.', existingReservation);
    }

    const serials = calculateNextSerials(master, reservations, locationCode);

    reservations.appendRow([
      new Date(),
      requestId,
      serials.totalSerial,
      serials.locationSerial,
      locationCode,
      projectLocation,
      'RESERVED',
      applicantName
    ]);

    SpreadsheetApp.flush();

    return jsonResponse(true, 'Central serial reserved.', {
      targetSheet: 'EAP_Master Sheet',
      totalSerialNumber: serials.totalSerial,
      individualSerialNumber: serials.locationSerial,
      locationCode: locationCode,
      totalApplicationCount: serials.totalNumber,
      locationApplicationCount: serials.locationNumber,
      centralOnly: true
    });
  } finally {
    lock.releaseLock();
  }
}

function submitRegistration(master, reservations, payload) {
  const requestId = String(payload.clientRequestId || '').trim();
  const projectLocation = String(payload.projectLocation || '').trim();
  const locationCode = String(payload.locationCode || '').trim() || locationCodeFor(projectLocation);

  if (!requestId) return jsonResponse(false, 'Client Request ID is missing.');
  if (!projectLocation || !locationCode || locationCode === 'NA') {
    return jsonResponse(false, 'Valid HAL Project Location is required.');
  }

  const lock = LockService.getScriptLock();
  lock.waitLock(30000);
  try {
    const existing = findMasterByRequestId(master, requestId);
    if (existing) {
      return jsonResponse(true, 'Already synced.', existing);
    }

    let reservation = findReservation(reservations, requestId);
    if (!reservation) {
      const serials = calculateNextSerials(master, reservations, locationCode);
      reservations.appendRow([
        new Date(),
        requestId,
        serials.totalSerial,
        serials.locationSerial,
        locationCode,
        projectLocation,
        'RESERVED',
        String(payload.name || '')
      ]);
      SpreadsheetApp.flush();
      reservation = {
        totalSerialNumber: serials.totalSerial,
        individualSerialNumber: serials.locationSerial,
        locationCode: locationCode,
        totalApplicationCount: serials.totalNumber,
        locationApplicationCount: serials.locationNumber
      };
    }

    const totalSerial = reservation.totalSerialNumber;
    const locationSerial = reservation.individualSerialNumber;
    const applicantName = String(payload.name || 'Applicant').trim();
    const place = projectPlace(projectLocation);
    const baseName = fileBaseName(applicantName, place, locationSerial);

    const rootFolder = getOrCreateCentralRootFolder();
    const locationFolder = getOrCreateChildFolder(rootFolder, locationCode + ' - ' + place);
    const applicantFolder = getOrCreateChildFolder(
      locationFolder,
      safeFileName(locationSerial.replace(/\//g, '-')) + ' - ' + safeFileName(applicantName)
    );

    let photoUrl = '';
    let signatureUrl = '';
    let pdfUrl = '';

    if (payload.photoBase64) {
      const photoFile = saveBase64FileToFolder(
        payload.photoBase64,
        applicantFolder,
        baseName + '_Photo.jpg',
        'image/jpeg'
      );
      photoUrl = photoFile.getUrl();
    }

    if (payload.signatureBase64) {
      const signatureFile = saveBase64FileToFolder(
        payload.signatureBase64,
        applicantFolder,
        baseName + '_Signature.jpg',
        'image/jpeg'
      );
      signatureUrl = signatureFile.getUrl();
    }

    if (payload.pdfBase64) {
      const pdfFile = saveBase64FileToFolder(
        payload.pdfBase64,
        applicantFolder,
        baseName + '.pdf',
        'application/pdf'
      );
      pdfUrl = pdfFile.getUrl();
    }

    const editableUrl = createEditableApplication(payload, applicantFolder, totalSerial, locationSerial, pdfUrl);
    const folderUrl = applicantFolder.getUrl();

    const aadhaarMasked = maskAadhaar(payload.idNumber || '');
    const photoFormula = photoUrl ? '=HYPERLINK("' + photoUrl + '","View Photo")' : '';
    const signatureFormula = signatureUrl ? '=HYPERLINK("' + signatureUrl + '","View Signature")' : '';
    const pdfFormula = pdfUrl ? '=HYPERLINK("' + pdfUrl + '","View / Download PDF")' : '';
    const editableFormula = editableUrl ? '=HYPERLINK("' + editableUrl + '","Edit Application")' : '';
    const folderFormula = '=HYPERLINK("' + folderUrl + '","Open Applicant Folder")';

    master.appendRow([
      new Date(),
      totalSerial,
      locationSerial,
      'Submitted',
      'Synced',
      projectLocation,
      locationCode,
      payload.eapDate || '',
      applicantName,
      payload.gender || '',
      payload.dob || '',
      payload.ageOnEapDate || '',
      payload.guardianName || '',
      payload.village || '',
      payload.panchayat || '',
      payload.city || place,
      payload.pinCode || '',
      payload.state || 'Karnataka',
      payload.mobile || '',
      payload.alternateMobile || '',
      payload.email || '',
      aadhaarMasked,
      payload.education || '',
      payload.occupation || '',
      payload.individualIncome || '',
      payload.category || '',
      payload.intention || '',
      payload.sector || payload.sectors || '',
      payload.msdpInterest || '',
      photoFormula,
      signatureFormula,
      pdfFormula,
      editableFormula,
      folderFormula,
      '',
      requestId
    ]);

    markReservationSubmitted(reservations, requestId);
    SpreadsheetApp.flush();

    const counts = submittedCounts(master, projectLocation);

    return jsonResponse(true, 'Saved to EAP_Master Sheet successfully.', {
      targetSheet: 'EAP_Master Sheet',
      totalSerialNumber: totalSerial,
      individualSerialNumber: locationSerial,
      totalApplicationCount: counts.total,
      locationApplicationCount: counts.location,
      pdfUrl: pdfUrl,
      photoUrl: photoUrl,
      signatureUrl: signatureUrl,
      editableUrl: editableUrl,
      folderUrl: folderUrl,
      pdfFileName: baseName + '.pdf',
      centralOnly: true
    });

  } finally {
    lock.releaseLock();
  }
}

function getOrCreateMasterSheet(ss) {
  let sheet = ss.getSheetByName(MASTER_SHEET_NAME);
  if (!sheet) sheet = ss.insertSheet(MASTER_SHEET_NAME);

  const headers = [
    'Submitted At','Total Serial Number','Location Serial Number','Application Status','Sync Status',
    'HAL Project Location','Location Code','EAP Date','Applicant Name','Gender','Date of Birth','Age',
    'Father / Husband / Mother Name','Address','Panchayat','City / Taluk','PIN Code','State',
    'Mobile Number','Alternate Mobile','Email ID','Aadhaar (Masked)','Highest Educational Qualification',
    'Occupation','Individual Income','Category','Intention','Business Sector','MSDP Interest',
    'Candidate Photo','Signature','Final PDF','Editable Application','Application Folder','Remarks',
    'Client Request ID'
  ];

  sheet.getRange(1, 1, 1, headers.length).setValues([headers]);
  sheet.setFrozenRows(1);
  sheet.getRange(1, 1, 1, headers.length)
    .setFontWeight('bold')
    .setBackground('#299932')
    .setFontColor('#FFFFFF');

  if (sheet.getMaxColumns() < headers.length) {
    sheet.insertColumnsAfter(sheet.getMaxColumns(), headers.length - sheet.getMaxColumns());
  }

  try { sheet.hideColumns(36); } catch (ignored) {}
  return sheet;
}

function getOrCreateReservationSheet(ss) {
  let sheet = ss.getSheetByName(RESERVATION_SHEET_NAME);
  if (!sheet) {
    sheet = ss.insertSheet(RESERVATION_SHEET_NAME);
  }

  const headers = [
    'Reserved At','Client Request ID','Total Serial','Location Serial',
    'Location Code','Project Location','Status','Applicant Name'
  ];
  sheet.getRange(1, 1, 1, headers.length).setValues([headers]).setFontWeight('bold');
  sheet.setFrozenRows(1);
  try { sheet.hideSheet(); } catch (ignored) {}
  return sheet;
}

function calculateNextSerials(master, reservations, locationCode) {
  let maxTotal = 0;
  let maxLocation = 0;

  if (master.getLastRow() >= 2) {
    const rows = master.getRange(2, 2, master.getLastRow() - 1, 2).getDisplayValues();
    rows.forEach(r => {
      maxTotal = Math.max(maxTotal, serialNumber(r[0]));
      if (String(r[1] || '').indexOf('/' + locationCode + '/') >= 0) {
        maxLocation = Math.max(maxLocation, serialNumber(r[1]));
      }
    });
  }

  if (reservations.getLastRow() >= 2) {
    const rows = reservations.getRange(2, 3, reservations.getLastRow() - 1, 5).getDisplayValues();
    rows.forEach(r => {
      maxTotal = Math.max(maxTotal, serialNumber(r[0]));
      if (String(r[2] || '') === locationCode) {
        maxLocation = Math.max(maxLocation, serialNumber(r[1]));
      }
    });
  }

  const totalNumber = maxTotal + 1;
  const locationNumber = maxLocation + 1;

  return {
    totalNumber: totalNumber,
    locationNumber: locationNumber,
    totalSerial: 'EAP/' + YEAR + '/' + String(totalNumber).padStart(2, '0'),
    locationSerial: 'EAP/' + YEAR + '/' + locationCode + '/' + String(locationNumber).padStart(2, '0')
  };
}

function submittedCounts(master, projectLocation) {
  if (master.getLastRow() < 2) return { total: 0, location: 0 };

  const rows = master.getRange(2, 2, master.getLastRow() - 1, 5).getDisplayValues();
  let total = 0;
  let location = 0;

  rows.forEach(r => {
    if (String(r[0] || '').trim()) {
      total++;
      if (String(r[4] || '').trim() === projectLocation) location++;
    }
  });

  return { total: total, location: location };
}

function findMasterByRequestId(master, requestId) {
  if (master.getLastRow() < 2) return null;
  const range = master.getRange(2, 36, master.getLastRow() - 1, 1);
  const match = range.createTextFinder(requestId).matchEntireCell(true).findNext();
  if (!match) return null;

  const row = match.getRow();
  return {
    totalSerialNumber: master.getRange(row, 2).getDisplayValue(),
    individualSerialNumber: master.getRange(row, 3).getDisplayValue(),
    pdfUrl: formulaUrl(master.getRange(row, 32).getFormula()),
    editableUrl: formulaUrl(master.getRange(row, 33).getFormula()),
    folderUrl: formulaUrl(master.getRange(row, 34).getFormula()),
    centralOnly: true
  };
}

function findReservation(sheet, requestId) {
  if (sheet.getLastRow() < 2) return null;
  const range = sheet.getRange(2, 2, sheet.getLastRow() - 1, 1);
  const match = range.createTextFinder(requestId).matchEntireCell(true).findNext();
  if (!match) return null;

  const row = match.getRow();
  return {
    totalSerialNumber: sheet.getRange(row, 3).getDisplayValue(),
    individualSerialNumber: sheet.getRange(row, 4).getDisplayValue(),
    locationCode: sheet.getRange(row, 5).getDisplayValue(),
    totalApplicationCount: serialNumber(sheet.getRange(row, 3).getDisplayValue()),
    locationApplicationCount: serialNumber(sheet.getRange(row, 4).getDisplayValue())
  };
}

function markReservationSubmitted(sheet, requestId) {
  if (sheet.getLastRow() < 2) return;
  const range = sheet.getRange(2, 2, sheet.getLastRow() - 1, 1);
  const match = range.createTextFinder(requestId).matchEntireCell(true).findNext();
  if (match) sheet.getRange(match.getRow(), 7).setValue('SUBMITTED');
}

function getOrCreateCentralRootFolder() {
  const props = PropertiesService.getScriptProperties();
  const savedId = String(props.getProperty('CENTRAL_ROOT_FOLDER_ID') || '').trim();

  if (savedId) {
    try { return DriveApp.getFolderById(savedId); } catch (ignored) {}
  }

  const folders = DriveApp.getFoldersByName(CENTRAL_ROOT_FOLDER_NAME);
  const folder = folders.hasNext() ? folders.next() : DriveApp.createFolder(CENTRAL_ROOT_FOLDER_NAME);
  props.setProperty('CENTRAL_ROOT_FOLDER_ID', folder.getId());
  return folder;
}

function getOrCreateChildFolder(parent, name) {
  const safe = safeFileName(name);
  const folders = parent.getFoldersByName(safe);
  return folders.hasNext() ? folders.next() : parent.createFolder(safe);
}

function saveBase64FileToFolder(base64Data, folder, fileName, mimeType) {
  const clean = String(base64Data).replace(/^data:[^;]+;base64,/, '');
  const bytes = Utilities.base64Decode(clean);
  const blob = Utilities.newBlob(bytes, mimeType, safeFileName(fileName));
  return folder.createFile(blob);
}

function createEditableApplication(payload, folder, totalSerial, locationSerial, pdfUrl) {
  const doc = DocumentApp.create(fileBaseName(
    payload.name || 'Applicant',
    projectPlace(payload.projectLocation || ''),
    locationSerial
  ) + '_Editable');

  const body = doc.getBody();
  body.appendParagraph('HAL EAP REGISTRATION').setHeading(DocumentApp.ParagraphHeading.HEADING1);
  body.appendParagraph('Entrepreneurship Awareness Programme (EAP)');
  body.appendParagraph('Sponsored by Hindustan Aeronautics Limited (HAL)');
  body.appendParagraph('Implemented by Entrepreneurship Development Institute of India (EDII)');
  body.appendParagraph('');
  body.appendParagraph('Application Serial No.: ' + totalSerial);
  body.appendParagraph('Location Serial No.: ' + locationSerial);
  body.appendParagraph('EAP Date: ' + (payload.eapDate || ''));
  body.appendParagraph('HAL Project Location: ' + (payload.projectLocation || ''));
  body.appendParagraph('');
  body.appendParagraph('Name: ' + (payload.name || ''));
  body.appendParagraph('Gender: ' + (payload.gender || ''));
  body.appendParagraph('Date of Birth: ' + (payload.dob || ''));
  body.appendParagraph('Age: ' + (payload.ageOnEapDate || ''));
  body.appendParagraph('Father / Husband / Mother Name: ' + (payload.guardianName || ''));
  body.appendParagraph('Address: ' + (payload.village || ''));
  body.appendParagraph('Panchayat: ' + (payload.panchayat || ''));
  body.appendParagraph('City / Taluk: ' + (payload.city || projectPlace(payload.projectLocation || '')));
  body.appendParagraph('PIN Code: ' + (payload.pinCode || ''));
  body.appendParagraph('State: ' + (payload.state || 'Karnataka'));
  body.appendParagraph('Mobile: ' + (payload.mobile || ''));
  body.appendParagraph('Alternate Mobile: ' + (payload.alternateMobile || ''));
  body.appendParagraph('Email: ' + (payload.email || ''));
  body.appendParagraph('Aadhaar: ' + maskAadhaar(payload.idNumber || ''));
  body.appendParagraph('Qualification: ' + (payload.education || ''));
  body.appendParagraph('Occupation: ' + (payload.occupation || ''));
  body.appendParagraph('Individual Income: ' + (payload.individualIncome || ''));
  body.appendParagraph('Category: ' + (payload.category || ''));
  body.appendParagraph('Intention: ' + (payload.intention || ''));
  body.appendParagraph('Business Sector: ' + (payload.sector || payload.sectors || ''));
  body.appendParagraph('MSDP Interest: ' + (payload.msdpInterest || ''));
  if (pdfUrl) {
    body.appendParagraph('');
    body.appendParagraph('Final PDF: ' + pdfUrl);
  }

  doc.saveAndClose();

  const file = DriveApp.getFileById(doc.getId());
  file.moveTo(folder);
  return file.getUrl();
}

function fileBaseName(applicantName, place, locationSerial) {
  const serialPart = String(locationSerial || '').replace(/\//g, '-');
  return safeFileName(applicantName) + '_' + safeFileName(place) + '_' + safeFileName(serialPart);
}

function safeFileName(name) {
  return String(name || 'EAP_File')
    .trim()
    .replace(/\s+/g, '_')
    .replace(/[\\/:*?"<>|#%]/g, '_')
    .replace(/_+/g, '_')
    .substring(0, 150);
}

function maskAadhaar(value) {
  const digits = String(value || '').replace(/\D/g, '');
  if (digits.length !== 12) return '';
  return 'XXXX XXXX ' + digits.substring(8);
}

function serialNumber(serial) {
  const parts = String(serial || '').split('/');
  const value = parseInt(parts[parts.length - 1], 10);
  return isNaN(value) ? 0 : value;
}

function formulaUrl(formula) {
  const match = String(formula || '').match(/HYPERLINK\("([^"]+)"/i);
  return match ? match[1] : '';
}

function locationCodeFor(projectLocation) {
  switch (String(projectLocation || '')) {
    case 'Kolar | Mulbagal': return 'K-M';
    case 'Kolar | Srinivaspur': return 'K-S';
    case 'Bengaluru Rural | Devanahalli': return 'BR-D';
    case 'Bengaluru Rural | Hoskote': return 'BR-H';
    case 'Tumkur | Tumkur': return 'T-T';
    case 'Tumkur | Gubbi': return 'T-G';
    case 'Tumkur | Sira': return 'T-S';
    case 'Bengaluru South | Ramanagara': return 'BS-R';
    case 'Bengaluru South | Channapatna': return 'BS-C';
    default: return 'NA';
  }
}

function projectPlace(projectLocation) {
  const value = String(projectLocation || '');
  const index = value.indexOf('|');
  return index >= 0 ? value.substring(index + 1).trim() : value.trim();
}

function jsonResponse(success, message, extra) {
  return ContentService
    .createTextOutput(JSON.stringify(Object.assign({
      success: success,
      message: message
    }, extra || {})))
    .setMimeType(ContentService.MimeType.JSON);
}
