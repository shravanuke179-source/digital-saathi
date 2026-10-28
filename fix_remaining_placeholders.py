from pathlib import Path
base=Path('app/src/main/java/com/example')
p=base/'ui/screens/ServicesScreen.kt';s=p.read_text(encoding='utf-8')
replacements={
'Text("VERIFIED",':'Text(if (userProfile.isVerified) "VERIFIED" else "NOT LINKED",',
'DOB: 12/04/1984 | Gender: Male':'Identity details require official verification',
'XXXX - XXXX - 8392':'No Aadhaar document linked',
'Consumer No: 04921-98762':'Consumer account not linked',
'Bill Period: August 2026':'Bill period: unavailable',
'Current Bill: ₹480.00':'Current bill: unavailable',
'Due Date: 18 September 2026':'Connect through the official electricity provider',
'Scheme: Antyodaya Anna Yojana (AAY)':'Scheme: confirm through the official ration portal',
'Monthly Quota: 35 Kg Grain (Wheat 20kg + Rice 15kg)':'Monthly quota: unavailable',
'Assigned FPS Shop: Palampur Fair Price Store':'Assigned FPS shop: unavailable',
'Village: Palampur | Survey/Gat No: 142/2':'Survey/Gat number: no land record linked',
'Total Area: 1 Hectare 24 Are (Cultivable)':'Total area: unavailable',
'Assessment: ₹24.50 | Status: Certified Digital Copy':'No certified digital copy linked',
'Portal service connected successfully for ${service.titleEn}. Official reference linked.':'Use the official provider for ${service.titleEn}. No provider account is linked.',
}
for old,new in replacements.items():s=s.replace(old,new)
p.write_text(s,encoding='utf-8')
# Remove unused action state from composables that only display information.
for name in ['SellProduceScreen.kt','FarmEquipmentScreen.kt']:
 p=base/'ui/screens'/name;s=p.read_text(encoding='utf-8')
 segments=s.split('@Composable')
 for i,segment in enumerate(segments):
  if 'cloudAction.launch' not in segment:
   segments[i]=segment.replace('    val cloudAction = rememberCloudAction()\n','')
 p.write_text('@Composable'.join(segments),encoding='utf-8')
p=base/'MainActivity.kt';s=p.read_text(encoding='utf-8')
s=s.replace('    val userProfile by repository.userProfile.collectAsState()', '    val userProfile by repository.userProfile.collectAsState()\n    val weather by repository.weatherData.collectAsState()')
s=s.replace('Weather is 28 degrees. Live wholesale APMC mandi rates for wheat, soybean, and cotton.', 'Weather: ${if (weather.isLiveApi) "${weather.temperature} degrees" else "currently unavailable"}. Browse published mandi rates.')
s=s.replace('आज का मौसम 28 डिग्री सेल्सियस है। गेहूं और सोयाबीन के ताज़ा थोक मंडी भाव उपलब्ध हैं।','मौसम और प्रकाशित मंडी भाव यहां देखें।')
s=s.replace('Verified citizen details and status of your submitted applications.','Citizen details and status of your saved applications.')
s=s.replace('सत्यापित आधार, राशन कार्ड, और जमा किए गए आवेदनों की स्थिति।','नागरिक जानकारी और सहेजे गए आवेदनों की स्थिति।')
p.write_text(s,encoding='utf-8')
