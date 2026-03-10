package net.furizon.gallery_processor.utils.extractExif;

import net.furizon.gallery_processor.dto.UploadExif;

public interface ExtractExif {
    UploadExif parseExif(String path);
}
