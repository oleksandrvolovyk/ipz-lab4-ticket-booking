package kpi.employees_backend

class PictureFileValidator(private val maxFileSizeInBytes: Int) {
    private val JPEGHeader = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    private val PNGHeader = byteArrayOf(
        0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(),
        0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte()
    )
    private val GIFHeader = byteArrayOf(
        0x47.toByte(), 0x49.toByte(), 0x46.toByte(), 0x38.toByte(),
        0x39.toByte(), 0x61.toByte()
    )

    operator fun invoke(fileBytes: ByteArray): Boolean {
        if (fileBytes.size > maxFileSizeInBytes || fileBytes.size <= 8) return false

        return when {
            checkHeaderMatch(fileBytes.sliceArray(JPEGHeader.indices), JPEGHeader) -> true
            checkHeaderMatch(fileBytes.sliceArray(PNGHeader.indices), PNGHeader) -> true
            checkHeaderMatch(fileBytes.sliceArray(GIFHeader.indices), GIFHeader) -> true
            else -> false
        }
    }

    private fun checkHeaderMatch(bytes: ByteArray, header: ByteArray): Boolean {
        return bytes contentEquals header
    }
}