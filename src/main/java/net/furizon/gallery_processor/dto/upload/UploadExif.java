package net.furizon.gallery_processor.dto.upload;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UploadExif extends UploadData {
    @Nullable private String cameraMaker;
    @Nullable private String cameraModel;
    @Nullable private String lensMaker;
    @Nullable private String lensModel;
    @Nullable private String focal;
    @Nullable private String shutter;
    @Nullable private String aperture;
    @Nullable private String iso;
}
