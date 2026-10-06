const SHEET_NAME = 'EAP Registrations';

function doPost(e) {
  try {
    const payload = JSON.parse((e && e.parameter && e.parameter.payload) || '{}');

    const props = PropertiesService.getScriptProperties();
    const expectedToken = props.getProperty('API_TOKEN');
    const sheetId = props.getProperty('SHEET_ID');
    const pdfFolderId = props.getProperty('PDF_FOLDER_ID');
    const photoFolderId = props.getProperty('PHOTO_FOLDER_ID');
    const signatureFolderId = props.getProperty('SIGNATURE_FOLDER_ID') || photoFolderId || pdfFolderId;

    if (!expectedToken || payload.token !== expectedToken) {
      return jsonResponse(false, 'Unauthorised request.');
    }
    if (!sheetId) {
      return jsonResponse(false, 'SHEET_ID is not configured.');
    }
    if (!pdfFolderId) {
      return jsonResponse(false, 'PDF_FOLDER_ID is not configured.');
    }

    const ss = SpreadsheetApp.openById(sheetId);
    let sheet = ss.getSheetByName(SHEET_NAME);
    if (!sheet) {
      sheet = ss.insertSheet(SHEET_NAME);
      addHeaders(sheet);
    } else if (sheet.getLastRow() === 0) {
      addHeaders(sheet);
    } else {
      ensureExtendedHeaders(sheet);
    }

    const requestId = String(payload.clientRequestId || '').trim();
    if (!requestId) {
      return jsonResponse(false, 'Client Request ID is missing.');
    }

    const existing = findExisting(sheet, requestId);
    if (existing) {
      return jsonResponse(true, 'Already synced.', {
        registrationId: existing.registrationId,
        pdfUrl: existing.pdfUrl
      });
    }

    const registrationId = createRegistrationId(sheet);
    let photoUrl = '';
    let pdfUrl = '';
    let signatureUrl = '';

    if (payload.photoBase64 && photoFolderId) {
      photoUrl = saveBase64File(
        payload.photoBase64,
        photoFolderId,
        registrationId + '.jpg',
        'image/jpeg'
      );
    }

    if (payload.pdfBase64) {
      const pdfName = safeFileName(payload.pdfFileName || (registrationId + '.pdf'));
      pdfUrl = saveBase64File(
        payload.pdfBase64,
        pdfFolderId,
        pdfName,
        'application/pdf'
      );
    }

    if (payload.signatureBase64 && signatureFolderId) {
      const ext = String(payload.signatureType || '').toLowerCase() === 'photo' ? '.jpg' : '.png';
      const mime = ext === '.jpg' ? 'image/jpeg' : 'image/png';
      signatureUrl = saveBase64File(
        payload.signatureBase64,
        signatureFolderId,
        registrationId + '_signature' + ext,
        mime
      );
    }

    sheet.appendRow([
      new Date(),
      requestId,
      registrationId,
      payload.eapDate || '',
      payload.name || '',
      payload.gender || '',
      payload.dob || '',
      payload.ageOnEapDate || '',
      payload.village || '',
      payload.panchayat || '',
      payload.block || '',
      payload.city || '',
      payload.state || '',
      payload.mobile || '',
      payload.alternateMobile || '',
      payload.email || '',
      payload.idType || '',
      payload.idNumber || '',
      payload.education || '',
      payload.occupation || '',
      payload.individualIncome || '',
      payload.category || '',
      payload.intention || '',
      payload.sectors || '',
      payload.msdpInterest || '',
      pdfUrl,
      photoUrl,
      payload.declarationAccepted === true ? 'Yes' : 'No',
      payload.projectLocation || '',
      payload.pinCode || '',
      payload.guardianName || '',
      payload.signatureType || '',
      signatureUrl
    ]);

    SpreadsheetApp.flush();

    return jsonResponse(true, 'Saved successfully.', {
      registrationId: registrationId,
      pdfUrl: pdfUrl,
      photoUrl: photoUrl,
      signatureUrl: signatureUrl
    });

  } catch (err) {
    return jsonResponse(false, 'Error: ' + err.message);
  }
}

function addHeaders(sheet) {
  sheet.appendRow([
    'Submitted At',
    'Client Request ID',
    'Registration ID',
    'EAP Date',
    'Name',
    'Gender',
    'DOB',
    'Age on EAP Date',
    'Village',
    'Panchayat',
    'Block',
    'City',
    'State',
    'Mobile Number',
    'Alternate Mobile Number',
    'Email ID',
    'Government ID Type',
    'Government ID Number',
    'Highest Educational Qualification',
    'Occupation',
    'Individual Income',
    'Category',
    'Intention',
    'Preferred Sector',
    'MSDP Interest',
    'PDF URL',
    'Photo URL',
    'Declaration Accepted',
    'HAL Project Location',
    'PIN Code',
    'Father Husband Mother Name',
    'Signature Type',
    'Signature URL'
  ]);
  sheet.setFrozenRows(1);
  sheet.getRange(1, 1, 1, 33).setFontWeight('bold');
}

function ensureExtendedHeaders(sheet) {
  const headers = [
    'HAL Project Location',
    'PIN Code',
    'Father Husband Mother Name',
    'Signature Type',
    'Signature URL'
  ];
  const currentLastColumn = sheet.getLastColumn();
  if (currentLastColumn < 33) {
    sheet.getRange(1, 29, 1, headers.length).setValues([headers]).setFontWeight('bold');
  }
}

function findExisting(sheet, requestId) {
  if (sheet.getLastRow() < 2) return null;
  const range = sheet.getRange(2, 2, sheet.getLastRow() - 1, 1);
  const match = range.createTextFinder(requestId).matchEntireCell(true).findNext();
  if (!match) return null;

  const row = match.getRow();
  return {
    registrationId: sheet.getRange(row, 3).getDisplayValue(),
    pdfUrl: sheet.getRange(row, 26).getDisplayValue()
  };
}

function createRegistrationId(sheet) {
  const tz = Session.getScriptTimeZone() || 'Asia/Kolkata';
  const year = Utilities.formatDate(new Date(), tz, 'yyyy');
  const serial = Math.max(sheet.getLastRow(), 1);
  return 'EAP-' + year + '-' + String(serial).padStart(5, '0');
}

function saveBase64File(base64Data, folderId, fileName, mimeType) {
  const clean = String(base64Data).replace(/^data:[^;]+;base64,/, '');
  const bytes = Utilities.base64Decode(clean);
  const blob = Utilities.newBlob(bytes, mimeType, safeFileName(fileName));
  const file = DriveApp.getFolderById(folderId).createFile(blob);
  return file.getUrl();
}

function safeFileName(name) {
  return String(name || 'EAP_File')
    .replace(/[\\/:*?"<>|#%]/g, '_')
    .substring(0, 150);
}

function jsonResponse(success, message, extra) {
  const body = Object.assign({
    success: success,
    message: message
  }, extra || {});

  return ContentService
    .createTextOutput(JSON.stringify(body))
    .setMimeType(ContentService.MimeType.JSON);
}
