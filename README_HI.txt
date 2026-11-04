OLD BAG STATEMENT MAKER — शुरुआती Android प्रोजेक्ट (Version 0.1)

इस संस्करण में:
- चार मिल और चार वित्तीय वर्ष चुनने का विकल्प
- मोबाइल कैमरे से बिल की फोटो
- Bundled English/Latin OCR मॉडल; इंस्टॉल के बाद OCR ऑफलाइन चल सकता है
- मूल फोटो फोन के app-specific storage में सुरक्षित
- OCR का निकला हुआ टेक्स्ट जाँच/सुधार के लिए स्क्रीन पर

महत्वपूर्ण सीमाएँ:
- यह अभी पूरा GST/Excel स्टेटमेंट बनाने वाला संस्करण नहीं है।
- अभी OCR टेक्स्ट को बिल नंबर/तारीख/मात्रा/GST/कुल रकम के संरचित कॉलम में बदलना, SQLite में सेव करना, duplicate जाँच, PDF/Excel export और backup/restore अगले चरण हैं।
- हर बिल की जानकारी मूल बिल से सत्यापित करें। यह GST return या tax invoice नहीं बनाता।
- फोन में इंटरनेट की जरूरत ऐप चलाने/OCR के लिए नहीं; पहली बार project build करने पर Gradle dependencies डाउनलोड करने के लिए इंटरनेट चाहिए।

Build:
1. Android Studio या AndroidIDE में इस project folder को खोलें।
2. Gradle sync करें (पहली बार इंटरनेट चाहिए)।
3. Build > Build APK(s) चुनें।
4. APK फोन में install करें और Camera permission दें.

Package: com.oldbag.statement
Minimum Android: 6.0 (API 23)
