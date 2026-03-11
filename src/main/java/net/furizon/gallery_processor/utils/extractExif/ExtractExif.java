package net.furizon.gallery_processor.utils.extractExif;

import net.furizon.gallery_processor.dto.upload.UploadData;

public interface ExtractExif {
    UploadData parseExif(String path);
}
