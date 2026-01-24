# 🚀 Release Notes - TiltLock v1.0.0

**Release Date:** January 24, 2026

We are excited to announce the initial release of **TiltLock**, a revolutionary security app that uses motion gestures to protect your privacy!

## 🌟 Highlights

### ✨ Invisible Authentication
- **Tilt-to-Unlock**: Replaced traditional keypads with a gesture-based system.
- **Customizable Patterns**: Users can now record and save their own secret tilt sequences.
- **Accurate Detection**: Implemented a "Neutral-Reset" state machine to prevent accidental triggers or ghost touches.

### 🔒 Military-Grade Vault
- **Secure File Storage**: Added ability to import and encrypt photos/videos from the gallery.
- **Encryption**: Utilizes `AES-256` with GCM mode via Android Keystore for robust security.
- **Safe Viewing**: Decrypts media in-memory for viewing without leaving trace files on the disk.

### 🎨 User Experience
- **Cyber-Noir UI**: Launched with a dark, high-contrast theme featuring neon accents.
- **Interactive Feedback**:
    - **Haptics**: Subtle vibrations guide the user through gesture stages.
    - **Visuals**: Dynamic parallax effects on the lock screen responding to device movement.
    - **Glitch Mode**: Special visual effects triggered upon unauthorized access attempts.

### 🛡️ Security Features
- **Break-In Logger**: Automatically logs timestamped failure events.
- **Intrusion Alerts**: Visual red-pulse alerts when a wrong pattern is entered.

---

## 🐛 Known Issues & Limitations
- **Gyroscope Dependency**: App strictly requires a device with a hardware gyroscope. It may not function on some low-end budget devices.
- **Emulator Support**: Motion gestures are difficult to simulate on standard Android Emulators; testing on a physical device is recommended.

---

*Thank you for using TiltLock! If you encounter any bugs, please report them on our GitHub Issues page.*
