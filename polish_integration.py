from pathlib import Path
import re
base=Path('app/src/main/java/com/example')
p=base/'data/repository/DigitalSaathiRepository.kt';s=p.read_text(encoding='utf-8')
for name in ['updateProfile','deleteProduceListing','selectBuyerForProduce','cancelEquipmentBooking']:
 s=s.replace('    fun '+name+'(', '    suspend fun '+name+'(')
 # these four were expression bodies in runMutation
 start=s.index('    suspend fun '+name+'(')
 end=s.index('\n    }',start)
 part=s[start:end].replace(' = runMutation {',' {')
 s=s[:start]+part+s[end:]
for item,flow in [('newApp','submittedApplications'),('newNotice','notices'),('grievance','grievances'),('newListing','produceListings'),('newRequest','deliveryRequests'),('newBooking','equipmentBookings')]:
 s=s.replace(f'listOf({item}) + _{flow}.value',f'listOf({item}) + _{flow}.value.filterNot {{ it.id == {item}.id }}')
s=s.replace('"phone" to initial.phone, "state" to initial.state))\n                                }','"phone" to initial.phone, "state" to initial.state))\n                                    Unit\n                                }')
start=s.index('    fun advanceDeliveryStatus(');end=s.index('    suspend fun deleteProduceListing(',start)
s=s[:start]+'''    fun advanceDeliveryStatus(requestId: String) = runMutation {
        val request = _deliveryRequests.value.first { it.id == requestId }
        val next = DeliveryStatus.entries.getOrNull(request.status.ordinal + 1)
            ?: error("This delivery is already complete.")
        com.google.firebase.functions.FirebaseFunctions.getInstance("asia-south1")
            .getHttpsCallable("updateDeliveryStatus")
            .call(mapOf("uid" to requireUid(), "deliveryId" to requestId, "status" to next.name))
            .awaitResult()
    }

'''+s[end:]
p.write_text(s,encoding='utf-8')

def wrap(s,marker,opening):
 at=s.index(marker);begin=s.rfind(opening,0,at)+len(opening)
 assert begin>=len(opening)
 level=1;end=begin
 while level:
  if s[end]=='{':level+=1
  if s[end]=='}':level-=1
  end+=1
 return s[:begin]+' cloudAction.launch {'+s[begin:end-1]+'} '+s[end-1:]
for file,items in {
'ui/screens/ProfileScreen.kt':[('repository.updateProfile(', 'onClick = {')],
'ui/screens/SellProduceScreen.kt':[('repository.deleteProduceListing(', 'onDeleteListing = { id ->'),('repository.selectBuyerForProduce(', 'onBuyerSelected = { buyer ->')],
'ui/screens/FarmEquipmentScreen.kt':[('repository.cancelEquipmentBooking(', 'onCancelBooking = { id ->')],
}.items():
 p=base/file;s=p.read_text(encoding='utf-8')
 if 'ProfileScreen' in file:
  s=s.replace('import androidx.compose.runtime.*','import androidx.compose.runtime.*\nimport com.example.ui.components.rememberCloudAction')
  s=re.sub(r'(fun ProfileScreen\([\s\S]*?\n\) \{)',r'\1\n    val cloudAction = rememberCloudAction()',s)
  s=s.replace('imageVector = Icons.Default.CheckCircle,\n                                    contentDescription = "Verified",\n                                    tint = ForestGreen,','imageVector = if (userProfile.isVerified) Icons.Default.CheckCircle else Icons.Default.Info,\n                                    contentDescription = if (userProfile.isVerified) "Verified" else "Not verified",\n                                    tint = if (userProfile.isVerified) ForestGreen else TextSecondaryMuted,')
 for marker,opening in items:s=wrap(s,marker,opening)
 p.write_text(s,encoding='utf-8')
p=base/'ui/components/SchemeDetailDialog.kt';s=p.read_text(encoding='utf-8').replace('Status: In Review by Block Officer','Saved in Digital Saathi. Submit via the official portal.').replace('आवेदन सफलतापूर्वक दर्ज किया गया','आवेदन सहेजा गया। आधिकारिक पोर्टल पर जमा करें।');p.write_text(s,encoding='utf-8')
p=Path('gradle/libs.versions.toml');s=p.read_text(encoding='utf-8');s=s.replace('firebase-auth = {','firebase-functions = { group = "com.google.firebase", name = "firebase-functions" }\nfirebase-appcheck-play-integrity = { group = "com.google.firebase", name = "firebase-appcheck-playintegrity" }\nfirebase-auth = {');p.write_text(s,encoding='utf-8')
p=Path('app/build.gradle.kts');s=p.read_text(encoding='utf-8').replace('  implementation(libs.firebase.auth)','  implementation(libs.firebase.auth)\n  implementation(libs.firebase.functions)\n  implementation(libs.firebase.appcheck.play.integrity)');p.write_text(s,encoding='utf-8')
p=base/'MainActivity.kt';s=p.read_text(encoding='utf-8');s=s.replace('        enableEdgeToEdge()','''        enableEdgeToEdge()
        runCatching {
            com.google.firebase.appcheck.FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                if (BuildConfig.DEBUG) com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
                else com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
''');p.write_text(s,encoding='utf-8')
