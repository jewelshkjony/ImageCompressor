# ImageCompressor
ImageCompressor is a non-visible MIT App Inventor extension written in Java that enables on-device image compression in JPEG, PNG, and WebP formats.


## Overview

**ImageCompressor** is a non-visible MIT App Inventor extension written in Java that enables on-device image compression in JPEG, PNG, and WebP formats. Designed for advanced developers, it provides high-performance, background-threaded processing, URI-to-path conversion, automatic naming, and event-driven callbacks to integrate seamlessly into complex App Inventor projects.

## Features

* **Multi-format Support**: Compress to JPEG, PNG, WebP (lossy).
* **Adjustable Quality**: Set compression level from 0 (max compression) to 100 (best quality).
* **Automatic File Management**: Creates a `CompressedImages` folder in external storage, resolves name collisions by appending incremental suffixes.
* **URI-to-File Conversion**: Accepts both content URIs and direct file paths.
* **Event Callbacks**: `ImageSelected`, `ImageSaved`, `ErrorOccurred` events for responsive, block-driven workflows.
* **Background Processing**: Runs compression in a separate thread to maintain UI responsiveness.
* **Optional Logging**: Enable detailed Logcat output via `LogEnabled(true)`.

## Installation

1. **Download AIX**: Copy `com.bosonshiggs.imagecompressor.aix` from the `out/` directory.
2. **Add to App Inventor**: In your App Inventor project, import the extension file (`.aix`).
3. **Set Permissions**: Ensure `WRITE_EXTERNAL_STORAGE` and `READ_EXTERNAL_STORAGE` permissions are granted in the packaged APK.

## Usage

### Block Interface

```blocks
// Initialize and enable logging
ImageCompressor1.LogEnabled(true)

// Trigger image picker (use ActivityStarter or similar)
... when ButtonPick clicked do
    call ActivityStarter1.StartActivity
end

// Handle selection
when ActivityStarter1.AfterActivity do
    call ImageCompressor1.PathFromUri(ActivityStarter1.ResultUri)
end

// Compress the chosen image
when ImageCompressor1.ImageSelected(path) do
    call ImageCompressor1.CompressImage(
        path,
        "compressed_photo",
        75,
        1001
    )
end

// Receive compressed file path
when ImageCompressor1.ImageSaved(id, savedPath) do
    // id = 1001, savedPath = "/storage/.../CompressedImages/compressed_photo.jpg"
    call Notifier1.ShowAlert("Saved to: " & savedPath)
end

// Handle errors
when ImageCompressor1.ErrorOccurred(errorMsg) do
    call Notifier1.ShowAlert("Compression error: " & errorMsg)
end
```

### Java API Reference

If you extend or inspect the `.java` source, the primary entry points are:

```java
// Enable or disable debug logging
imageCompressor.LogEnabled(true);

// Convert URI to file path
String path = imageCompressor.PathFromUri(uriString);

// Compress on background thread
imageCompressor.CompressImage(
    "/storage/.../photo.png",
    "outputName",
    90,
    42 // developer-defined ID
);

// Implement callbacks in ActivityResultListener
@Override
public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
        Uri uri = data.getData();
        String path = imageCompressor.PathFromUri(uri.toString());
        imageCompressor.ImageSelected(path);
    }
}

// Events dispatched back to blocks
public void ImageSelected(String imagePath) { /* ... */ }
public void ImageSaved(int id, String filePath) { /* ... */ }
public void ErrorOccurred(String errorMessage) { /* ... */ }
```

## Customization

* **Directory Name**: Modify `myDirName` constant in `ImageCompressor.java`.
* **Supported Formats**: Add support for other `Bitmap.CompressFormat` types as needed.
* **Threading**: Replace `new Thread(...)` with your own `ExecutorService` for advanced concurrency control.

## Development & Build

Clone the repository and use Fast CLI to build:

```bash
git clone git@github.com:iagolirapasssos/ImageCompressor.git
cd ImageCompressor/image-compressor
fast build -o
```

Artifacts:

* `out/com.bosonshiggs.imagecompressor.aix` — extension file.
* `docs/` — generated Javadoc and usage markdown.

## Contributing

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/my-extension-update`.
3. Commit your changes with tests.
4. Build and verify with `fast build -o`.
5. Open a Pull Request describing your enhancements.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

*For more information, visit the [App Inventor community](https://community.appinventor.mit.edu/).*
