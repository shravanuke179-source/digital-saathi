from pathlib import Path
import re
base=Path('app/src/main/java/com/example')
configs={
'ui/components/SchemeDetailDialog.kt':['onApplySubmitted(scheme,'],
'ui/components/GrievanceDialog.kt':['onSubmit(category,'],
'ui/components/PostNoticeDialog.kt':['onPostNotice(title,'],
'ui/screens/SellProduceScreen.kt':['val finalCropName =','onDeliveryRequested(\n'],
'ui/screens/FarmEquipmentScreen.kt':['onBookingConfirmed(\n'],
}
for file,markers in configs.items():
 p=base/file;s=p.read_text(encoding='utf-8')
 if '/screens/' in file:s=s.replace('import androidx.compose.runtime.*','import androidx.compose.runtime.*\nimport com.example.ui.components.rememberCloudAction')
 # all affected composables receive their own remembered action (no shared form state)
 s=re.sub(r'((?:private )?fun \w+\([\s\S]*?\n\) \{)',r'\1\n    val cloudAction = rememberCloudAction()',s)
 for name in ['onApplySubmitted','onSubmit','onPostNotice','onDeliveryRequested','onBookingConfirmed']:
  s=re.sub(r'('+name+r': )\(',r'\1suspend (',s)
 if 'SchemeDetail' in file:
  s=s.replace('onApplySubmitted: suspend (Scheme, String, String, String) -> Unit','onApplySubmitted: suspend (Scheme, String, String, String) -> String')
  s=s.replace('val appId = "APP-${(100000..999999).random()}"\n                                generatedAppId = appId\n                                onApplySubmitted(scheme, applicantName, applicantPhone, applicantAadhaar)','generatedAppId = onApplySubmitted(scheme, applicantName, applicantPhone, applicantAadhaar)')
  s=s.replace('mutableStateOf("XXXX-XXXX-8392")','mutableStateOf("")')
 if 'Grievance' in file:
  s=s.replace('onSubmit: suspend (String, String, String) -> Unit','onSubmit: suspend (String, String, String) -> String')
  s=s.replace('tokenNumber = "GRV-${(1000..9999).random()}"\n                                onSubmit(category, subject, description)','tokenNumber = onSubmit(category, subject, description)')
 for marker in markers:
  at=s.index(marker);begin=s.rfind('onClick = {',0,at)+len('onClick = {')
  assert begin>=len('onClick = {')
  level=1;end=begin
  while level:
   if s[end]=='{':level+=1
   if s[end]=='}':level-=1
   end+=1
  s=s[:begin]+' cloudAction.launch {'+s[begin:end-1]+'} '+s[end-1:]
 s=s.replace('quantityText.toDoubleOrNull() ?: 10.0','quantityText.toDoubleOrNull() ?: error("Enter a valid quantity.")').replace('expectedPriceText.toDoubleOrNull() ?: 3000.0','expectedPriceText.toDoubleOrNull() ?: error("Enter a valid price.")')
 p.write_text(s,encoding='utf-8')
p=base/'MainActivity.kt';s=p.read_text(encoding='utf-8').replace('repository.submitSchemeApplication(s, name, phone, aadhaar)','repository.submitSchemeApplication(s, name, phone, aadhaar).id');p.write_text(s,encoding='utf-8')
p=base/'ui/screens/VillageSosScreen.kt';s=p.read_text(encoding='utf-8').replace('repository.submitGrievance(cat, sub, desc)','repository.submitGrievance(cat, sub, desc).id');p.write_text(s,encoding='utf-8')
