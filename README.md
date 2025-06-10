# 🌱 EcoZym - Aplikasi Mobile Manajemen Limbah Terintegrasi

<div align="center">

**Transformasi Limbah Organik Menjadi Eco-Enzyme Bernilai Ekonomis**

</div>

## 📖 Tentang EcoZym

EcoZym adalah platform berbasis mobile yang dirancang untuk mengatasi tantangan pengelolaan limbah di Indonesia dengan mengubah limbah organik, khususnya kulit buah, menjadi eco-enzyme yang bernilai ekonomis. Berbeda dari platform marketplace konvensional, EcoZym mengadopsi pendekatan layanan end-to-end yang mengintegrasikan pengumpulan, pengolahan, dan distribusi produk olahan limbah.

### 🎯 Visi & Misi

**Visi:** Menjadi solusi digital terdepan dalam pengelolaan limbah berkelanjutan di Indonesia

**Misi:** 
- Mengintegrasikan seluruh stakeholder dalam ekosistem pengelolaan limbah
- Meningkatkan transparansi dan efisiensi operasional melalui teknologi
- Mendukung implementasi ekonomi sirkular melalui inovasi digital

## 🌍 Latar Belakang

Pengelolaan limbah di Indonesia menghadapi tantangan signifikan:
- **175.000 ton** limbah dihasilkan setiap hari
- Hanya **69%** yang dikelola dengan baik (Kementerian LHK, 2021)
- Potensi ekonomi limbah yang belum termanfaatkan optimal

EcoZym hadir sebagai solusi inovatif untuk mengubah paradigma pengelolaan limbah dari beban menjadi aset bernilai ekonomis.

## ✨ Fitur Utama

### 👥 Untuk User/Company Role

<details>
<summary><strong>🔐 Authentication dan Registration System</strong></summary>

- Multi-stage registration process dengan tiga tahapan verifikasi
- Upload dan verifikasi dokumen perusahaan (SIUP/TDP, NIB)
- Admin confirmation system sebelum akun dapat digunakan
- Secure login dengan role-based access control
</details>

<details>
<summary><strong>📊 Dashboard Management</strong></summary>

- Tampilan overview jumlah sampah yang telah diregistrasikan
- Statistik limbah dan eco-enzyme yang diterima perusahaan
- Quick access ke fitur-fitur utama aplikasi
</details>

<details>
<summary><strong>🗂️ Waste Registration System</strong></summary>

- Form registrasi limbah dengan input jumlah (kg) dan lokasi
- Automatic pricing calculation berdasarkan waste type
- Integration dengan pricing guide system
- Confirmation dan tracking order number
</details>

<details>
<summary><strong>📍 Waste Pickup Tracking</strong></summary>

- Real-time status tracking dengan tahapan: In Transit, Processing, Returning
- Timeline visualization untuk setiap order
- Detail informasi pickup dan delivery
- Notification system untuk status updates
</details>

<details>
<summary><strong>📈 Reporting dan Analytics</strong></summary>

- Grafik statistik jumlah limbah yang telah diregistrasikan
- Tracking jumlah eco-enzyme yang telah diterima
- Historical data dan trend analysis
- Export functionality untuk laporan
</details>

<details>
<summary><strong>💰 Waste Pricing Guide</strong></summary>

- Transparent pricing structure berdasarkan jenis limbah
- Real-time price information
- Historical pricing trends
- Calculation tool untuk estimasi biaya
</details>

<details>
<summary><strong>👤 Profile Management</strong></summary>

- Company profile management
- Account settings dan preferences
- Document management dan renewal
- Contact information updates
</details>

### 👨‍💼 Untuk Admin Role

<details>
<summary><strong>📊 Admin Dashboard</strong></summary>

- Total limbah terdaftar dalam sistem
- Eco-enzyme production overview
- Jumlah perusahaan yang terdaftar
- Key performance indicators dan metrics
</details>

<details>
<summary><strong>⏳ Progress Management</strong></summary>

- Monitoring progress pengolahan limbah per perusahaan secara real-time
- Status tracking untuk semua active orders dengan 5 tahapan utama:
  - **Scheduled**: Pesanan telah dikonfirmasi dan dijadwalkan untuk pickup
  - **In Transit**: Limbah sedang dalam perjalanan menuju fasilitas pengolahan
  - **Processing**: Limbah sedang dalam tahap pengolahan menjadi eco-enzyme
  - **Returning**: Eco-enzyme sedang dalam perjalanan kembali ke perusahaan
  - **Delivered**: Eco-enzyme telah berhasil diterima oleh perusahaan client
</details>

<details>
<summary><strong>⚙️ Production Management</strong></summary>

- Batch processing dengan kode unik (ECO-2025-075, ECO-2025-074, dll.)
- Multi-stage status tracking: Material Collection, Fermentation, Filtering, Purification, Ready for Return
- Timeline pengolahan dengan estimasi waktu penyelesaian
- Quality control checkpoints dan documentation
</details>

<details>
<summary><strong>📦 Order Management</strong></summary>

- Status management: Scheduled, In Transit, Processing, Returning, Delivered
- Assignment dan reassignment orders ke partners/drivers
- Route optimization dan scheduling
- Communication tools dengan stakeholders
</details>

<details>
<summary><strong>👥 User Management</strong></summary>

- Multi-role system: Admin, Company, Driver
- Company registration dengan verifikasi dokumen
- Performance monitoring dan rating system
</details>

<details>
<summary><strong>📊 Reporting & Analytics</strong></summary>

- Comprehensive monthly reports dengan visualisasi data
- Environmental impact tracking dan pengurangan limbah
- Operational metrics: rata-rata waktu processing, load size
- Waste distribution analysis berdasarkan jenis dan lokasi
</details>

## 🛠️ Teknologi

| Kategori | Teknologi | Deskripsi |
|----------|-----------|-----------|
| **Platform** | Android (API Level 24+) | Platform mobile utama |
| **Bahasa** | Kotlin + Coroutines | Bahasa pemrograman utama dengan dukungan asynchronous |
| **Arsitektur** | MVVM + Clean Architecture | Pola arsitektur untuk skalabilitas dan maintainability |
| **UI Framework** | Material Design Components | Komponen antarmuka pengguna modern |
| **Navigation** | Android Navigation Component | Sistem navigasi antar fragment |
| **Database** | Firebase Firestore | Database real-time NoSQL |
| **Authentication** | Firebase Auth + Multi-Factor | Sistem autentikasi dengan keamanan berlapis |
| **File Storage** | Cloudinary | Penyimpanan dan manajemen file media |
| **Cloud Functions** | Firebase Functions | Logika bisnis server-side |
| **Analytics** | Firebase Analytics | Pelacakan perilaku pengguna |
| **Maps** | Google Maps SDK | Layanan peta dan lokasi |
| **Charts** | MPAndroidChart | Visualisasi data dan grafik |
| **Image Processing** | Glide + Cloudinary | Pemrosesan dan loading gambar |
| **Push Notifications** | Firebase Cloud Messaging | Sistem notifikasi push |
| **Dependency Injection** | Hilt | Manajemen dependensi |
| **IDE** | Android Studio | Integrated Development Environment |
| **Build System** | Gradle with Kotlin DSL | Sistem build automation |
| **Version Control** | Git with GitFlow | Kontrol versi dan workflow |
| **Testing** | JUnit, Espresso, Mockito | Framework testing komprehensif |
| **Code Quality** | Detekt | Analisis statis kode |

## 🚀 Instalasi dan Setup

1. **Clone repository ini**
   ```bash
   git clone https://github.com/your-username/ecozym.git
   cd ecozym
   ```

2. **Buka project di Android Studio**
   - File → Open → Pilih folder project

3. **Setup Firebase configuration**
   - Download `google-services.json` dari Firebase Console
   - Letakkan file tersebut di folder `app/`

4. **Sync project dengan Gradle**
   - Klik "Sync Now" ketika diminta oleh Android Studio

5. **Build dan run aplikasi**
   - Klik tombol Run atau tekan `Shift + F10`

## 📊 Manfaat

### 🎓 Manfaat Teoritis
- Kontribusi pada pengembangan aplikasi mobile untuk keberlanjutan lingkungan
- Framework integrasi cloud computing dalam sistem manajemen limbah
- Model bisnis digital untuk ekonomi sirkular

### 💼 Manfaat Praktis
- Peningkatan efisiensi operasional hingga **40%**
- Transparansi penuh dalam pricing dan proses
- Implementasi ekonomi sirkular melalui digitalisasi
- Fasilitasi program sustainability perusahaan

## 🔧 Batasan

- **Platform:** Fokus pada Android menggunakan Kotlin
- **Database:** Firebase Firestore dan Cloudinary
- **Target:** Limbah organik & non-organik sektor komersial/industri  
- **Region:** Implementasi utama di Indonesia (Bahasa Indonesia)

## 👥 Tim Pengembang - Kelompok 11

| Nama | NIM | 
|------|-----|
| **Fadli Ahmad Yazid** | 2208107010032 |
| **Rizky Yusmansyah** | 2208107010024 |

## 🏛️ Institusi

**Jurusan Informatika**  
Fakultas Matematika dan Ilmu Pengetahuan Alam  
Universitas Syiah Kuala  
Darussalam, Banda Aceh, 2025

---

<div align="center">

**🌱 Mari Bersama Membangun Masa Depan yang Berkelanjutan! 🌱**

[⭐ Star](https://github.com/your-username/ecozym) | [🐛 Report Bug](https://github.com/your-username/ecozym/issues) | [💡 Request Feature](https://github.com/your-username/ecozym/issues)

</div>
