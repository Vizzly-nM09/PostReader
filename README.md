# PostReader - Aplikasi Android REST API Client

> Tugas Pemrograman Bergerak (Mobile Programming) - Bab 9: _Networking with Retrofit_  
> Dosen Pengampu: **Syaeful Anas Aklani, S.Kom., M.Kom.**

---

## 1. Identitas Mahasiswa

| Informasi         | Keterangan                                     |
| :---------------- | :--------------------------------------------- |
| **Nama Lengkap**  | Fadhlan Limanda                                |
| **NPM**           | 2532066                                        |
| **Kelas**         | 3TIMA                                          |
| **Program Studi** | [Teknologi Informasi]                          |
| **Mata Kuliah**   | Mobile Programming                             |
| **Topik Modul**   | Bab 9: Networking with Retrofit & JSON Parsing |

---

## 2. Kesesuaian Capaian Pembelajaran (Sub-CPMK)

Aplikasi ini dibangun untuk memenuhi capaian pembelajaran pada silabus modul Chapter 9:

| No  | Sub-CPMK         | Keterangan Modul                                                              | Implementasi pada Aplikasi PostReader                                                                                                                                                                         |
| :-: | :--------------- | :---------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
|  1  | **Sub-CPMK 3.5** | Memahami konsep dasar REST API dan implementasi HTTP Client                   | Konfigurasi Retrofit di `ApiClient`, pendefinisian REST kontrak di `ApiService`, serta implementasi HTTP Methods (`GET`, `POST`, `PUT`, `DELETE`).                                                            |
|  2  | **Sub-CPMK 3.6** | Mengimplementasikan parsing JSON respons dan menampilkan data ke RecyclerView | Serialisasi dan deserialisasi JSON otomatis menggunakan `GsonConverterFactory`, eksekusi asynchronous via `call.enqueue()`, serta penyajian data melalui `RecyclerView` dengan custom Adapter dan ViewHolder. |

---

## 3. Checklist Kebutuhan Tugas & Fitur

### Persyaratan Dasar Modul

- [x] Dependensi Retrofit 2 dan Gson Converter terpasang pada Gradle
- [x] Izin jaringan (`android.permission.INTERNET` dan `ACCESS_NETWORK_STATE`) di `AndroidManifest.xml`
- [x] Inisialisasi Retrofit client singleton dengan Base URL JSONPlaceholder
- [x] Interface `ApiService` untuk pemanggilan API
- [x] Menampilkan daftar data ke `RecyclerView`
- [x] Navigasi detail item ke `DetailActivity`
- [x] Penanganan status UI (_Loading ProgressBar_, _Empty State_, dan _Error/Failure State_)

### Peningkatan & Fitur Tambahan (Beyond Basic Starter)

- [x] **Operasi CRUD Lengkap**: Tidak hanya membaca data (`GET`), pengguna dapat menambah post baru (`POST`), memperbarui isi post (`PUT`), dan menghapus post (`DELETE`).
- [x] **Relasi Data Multi-Resource**:
  - Menampilkan profil penulis postingan (`GET /users/{userId}`) di kartu detail.
  - Menampilkan daftar komentar pembaca (`GET /posts/{id}/comments`) di bawah artikel.
- [x] **Multi-Tab Resource (Bottom Navigation)**:
  - Tab 1: **Posts** (Artikel blog dan diskusi komentar).
  - Tab 2: **Todos** (Daftar tugas dengan filter chip _Semua_, _Tertunda_, dan _Selesai_).
- [x] **Sinkronisasi Data Lokal (In-Memory Repository)**: Data yang dibuat atau disunting tetap tersinkronisasi antar-halaman selama sesi aplikasi berjalan, mengatasi batasan API mock publik yang tidak menyimpan perubahan permanen di server.
- [x] **Pencarian Realtime & Swipe-to-Refresh**: Filter pencarian instan pada judul post serta penarikan layar untuk memuat ulang data.
- [x] **Sistem Desain Antarmuka Hangat**: Tema visual berbasis palet _Warm Amber & Cream_ yang nyaman dibaca dan memenuhi standar aksesibilitas kontras WCAG AA.

---

## 4. Dokumentasi Endpoint API

Base URL yang digunakan: `https://jsonplaceholder.typicode.com/`

| HTTP Method | Endpoint               | Return Type     | Deskripsi                                                         |
| :---------- | :--------------------- | :-------------- | :---------------------------------------------------------------- |
| `GET`       | `/posts`               | `List<Post>`    | Mengambil seluruh daftar postingan artikel (100 item)             |
| `GET`       | `/posts/{id}`          | `Post`          | Mengambil detail satu postingan berdasarkan ID                    |
| `POST`      | `/posts`               | `Post`          | Mengirim data postingan baru ke server                            |
| `PUT`       | `/posts/{id}`          | `Post`          | Memperbarui judul dan isi postingan yang ada                      |
| `DELETE`    | `/posts/{id}`          | `Void`          | Menghapus postingan dari sistem                                   |
| `GET`       | `/users/{id}`          | `User`          | Mengambil data profil penulis (nama, username, email, perusahaan) |
| `GET`       | `/posts/{id}/comments` | `List<Comment>` | Mengambil daftar komentar terkait postingan                       |
| `GET`       | `/todos`               | `List<Todo>`    | Mengambil daftar tugas (todos)                                    |
| `POST`      | `/todos`               | `Todo`          | Menambahkan item tugas baru                                       |
| `PUT`       | `/todos/{id}`          | `Todo`          | Memperbarui status selesai/belum selesai dari tugas               |
| `DELETE`    | `/todos/{id}`          | `Void`          | Menghapus item tugas                                              |

---

## 5. Struktur Direktori Proyek

```text
com.example.postreader/
├── adapter/
│   ├── CommentAdapter.java      # Adapter untuk daftar komentar postingan
│   ├── PostAdapter.java         # Adapter untuk daftar artikel postingan
│   └── TodoAdapter.java         # Adapter untuk daftar tugas dan toggle checkbox
├── Model/
│   ├── Comment.java             # POJO model respons komentar
│   ├── Post.java                # POJO model respons postingan
│   ├── Todo.java                # POJO model respons tugas
│   └── User.java                # POJO model respons data profil pengguna
├── network/
│   ├── ApiClient.java           # Singleton Retrofit builder & OkHttpClient
│   └── ApiService.java          # Deklarasi antarmuka endpoint Retrofit
├── repository/
│   ├── PostRepository.java      # Pengelola cache dan sinkronisasi CRUD postingan
│   └── TodoRepository.java      # Pengelola cache dan status tugas
├── CreateEditPostActivity.java  # Form penambahan dan pengeditan postingan
├── DetailActivity.java          # Layar rincian post, profil penulis, dan komentar
└── MainActivity.java            # Layar utama (daftar post, tab todos, pencarian)
```

---

## 6. Tangkapan Layar Aplikasi

> Letakkan gambar tangkapan layar pengujian aplikasi Anda di dalam folder `docs/screenshots/` atau lampirkan di bawah ini:

| 1. Daftar Postingan | 2. Detail Post & Komentar | 3. Form Buat / Edit Post | 4. Tab Todos & Filter |
| :-----------------: | :-----------------------: | :----------------------: | :-------------------: |
| _(Tangkapan Layar)_ |    _(Tangkapan Layar)_    |   _(Tangkapan Layar)_    |  _(Tangkapan Layar)_  |

---

## 7. Prasyarat & Cara Menjalankan

### Prasyarat Sistem

1. **Android Studio** (Ladybug / Meerkat atau versi yang lebih baru)
2. **Java Development Kit (JDK)**: JDK 17 atau 21 (disarankan menggunakan JBR bawaan Android Studio)
3. **Android SDK**: Min SDK 24 (Android 7.0 Nougat), Target SDK 37
4. **Koneksi Internet** aktif untuk memuat data dari JSONPlaceholder API

### Langkah Menjalankan Aplikasi

1. Buka Android Studio, pilih menu **File > Open**, lalu arahkan ke direktori proyek ini:
   ```text
   PostReader/
   ```
2. Tunggu proses **Gradle Sync** hingga selesai.
3. Hubungkan perangkat fisik Android melalui USB Debugging atau jalankan Android Virtual Device (AVD).
4. Klik tombol **Run 'app'** (`Shift + F10`) di toolbar Android Studio.
5. Anda juga dapat memverifikasi unit test aplikasi melalui terminal:
   ```bash
   ./gradlew testDebugUnitTest
   ```
