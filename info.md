### 📄 **Pdf Dosyasının Okunarak Excel Çıktısı Haline Getirilmesi** 📊

#### Kullanıcı **/api/pdfToExcel** endpointine istek attığında sırasıyla şu işlemler gerçekleşir:

1. **📸 Pdf Dosyasının Görsel Olarak Okunması:**
    - Kullanıcıdan gelen PDF dosyası, **image** olarak okuma işlemine tabi tutulur.
    - Kaç sayfa PDF gönderildiyse, her bir sayfanın resmi çekilir ve bir **liste** haline getirilir.

2. **🔍 OCR İşlemi ve Metin İşleme:**
    - Çekilen resimler, bir `for` döngüsü ile dönülerek her biri **OCR** (Optical Character Recognition) teknolojisiyle okunur ve **metne dönüştürülür**.
    - Sonrasında, bu metin içerisinde istenilen bilgiler **regex** (düzenli ifadeler) kullanılarak alınır ve bir **class**a maplenir.
    - Elde edilen veriler bir **liste** halinde toplanır.

3. **📈 Excel Dosyasının Oluşturulması:**
    - Liste halindeki bu **class**, **Excel Generator**'a gönderilir.
    - **Excel Generator**, bu class’taki verileri kullanarak Excel tablosunun **satırlarını oluşturur**.

4. **💾 Excel Dosyasının Kaydedilmesi ve URL'nin Client'a Dönülmesi:**
    - Tüm işlemler tamamlandığında, oluşturulan **Excel dosyası** **excelFiles** klasörüne kaydedilir.
    - Bu dosyanın **URL’si**, client’a döndürülerek dosyanın indirilebilmesi sağlanır.
