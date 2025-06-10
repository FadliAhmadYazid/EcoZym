# EcoZym Mobile Application

EcoZym adalah aplikasi mobile inovatif yang menghubungkan perusahaan dengan layanan pengolahan limbah organik menjadi eco-enzyme. Aplikasi ini memfasilitasi proses registrasi limbah, tracking pengolahan, dan monitoring produksi eco-enzyme secara real-time.

## 🌱 Tentang EcoZym

EcoZym berkomitmen untuk mengurangi limbah organik dan mengubahnya menjadi eco-enzyme yang bermanfaat untuk lingkungan. Aplikasi ini menyediakan platform yang mudah digunakan untuk perusahaan dalam mengelola limbah organik mereka secara berkelanjutan.

## 🛠️ Teknologi yang Digunakan

- **Platform**: Android Studio
- **Bahasa Pemrograman**: Kotlin
- **Database & Backend**: Firebase
  - Firebase Authentication
  - Firebase Firestore Database
  - Firebase Storage
  - Firebase Cloud Functions

## 👥 Peran Pengguna

### User/Company Role

#### 1. Registrasi Akun (3 Tahap)
- **Tahap 1**: Informasi dasar perusahaan
- **Tahap 2**: Detail kontak dan lokasi
- **Tahap 3**: Upload dokumen verifikasi perusahaan
- Menunggu konfirmasi admin sebelum dapat login

#### 2. Dashboard Utama
- Menampilkan total sampah yang telah diregistrasikan
- Overview aktivitas limbah perusahaan
- Statistik ringkas penggunaan layanan

#### 3. Waste Register
- Form pendaftaran limbah organik
- Input jumlah limbah (dalam kg)
- Penentuan lokasi pickup
- Integrasi dengan pricing guide untuk estimasi biaya

#### 4. Waste Pickup Tracking
- Monitoring status pesanan real-time:
  - **In Transit**: Limbah sedang dalam perjalanan
  - **Processing**: Limbah sedang diolah menjadi eco-enzyme
  - **Returning**: Eco-enzyme siap dikirim kembali
- Detail informasi setiap tahap proses

#### 5. Report & Analytics
- Grafik statistik limbah yang telah diregistrasikan
- Data jumlah eco-enzyme yang telah diterima

#### 6. Waste Pricing Guide
- Panduan harga berdasarkan jenis dan jumlah limbah
- Informasi paket layanan yang tersedia

#### 7. Profile Management
- Pengaturan informasi profil perusahaan
- Riwayat transaksi
- Pengaturan notifikasi

### Admin Role

#### 1. Dashboard Admin
- Total limbah terdaftar di sistem
- Monitoring produksi eco-enzyme
- Jumlah perusahaan yang terdaftar
- Overview operasional keseluruhan

#### 2. Progress Management
Monitoring progress pengolahan limbah dengan 5 status:
- **Scheduled**: Pesanan telah dijadwalkan
- **In Transit**: Dalam perjalanan untuk pickup
- **Processing**: Sedang dalam tahap pengolahan
- **Returning**: Dalam proses pengiriman kembali
- **Delivered**: Telah diterima oleh perusahaan

#### 3. Production Management
- **Batch Processing**: Sistem kode unik (ECO-2025-075, ECO-2025-074, dll.)
- **Status Tracking Produksi**:
  - Material Collection
  - Fermentation
  - Filtering
  - Purification
  - Ready for Return
- Timeline pengolahan dengan estimasi waktu penyelesaian

#### 4. User Management
- **Multi-role System**: Admin, Company, Driver
- **User Registration**: Verifikasi mitra
- **User Management**: Sistem manajemen pengguna dengan berbagai tingkat akses
- Approval sistem untuk registrasi perusahaan baru

#### 5. Reporting & Analytics
- **Comprehensive Reports**: Laporan bulanan dengan visualisasi data
- **Environmental Impact**: Tracking dampak lingkungan dan pengurangan limba

## 🚀 Fitur Utama

### Untuk Perusahaan:
- ✅ Registrasi limbah yang mudah dan cepat
- 📊 Dashboard monitoring komprehensif
- 🚚 Real-time tracking pickup dan processing
- 💰 Transparansi pricing dan estimasi biaya
- 📈 Analytics dan reporting terdetail

### Untuk Admin:
- 🎛️ Control panel manajemen operasional
- 📋 Sistem batch processing yang efisien
- 👥 Multi-user management system
- 📊 Advanced analytics dan environmental impact tracking
- 🔄 End-to-end process monitoring

## 🌍 Dampak Lingkungan

EcoZym membantu perusahaan dalam:
- Mengurangi limbah organik yang berakhir di TPA
- Menghasilkan eco-enzyme yang ramah lingkungan
- Menciptakan ekonomi sirkular dalam pengelolaan limbah
- Monitoring dan pelaporan dampak lingkungan secara real-time

## 📱 Instalasi dan Setup

1. Clone repository ini
2. Buka project di Android Studio
3. Setup Firebase configuration
4. Sync project dengan Gradle
5. Build dan run aplikasi

## 📄 Lisensi

Copyright © 2025 EcoZym. All rights reserved.

---

**EcoZym - Transforming Waste, Creating Value** 🌱
