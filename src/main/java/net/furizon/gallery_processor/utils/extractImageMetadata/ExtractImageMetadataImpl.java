package net.furizon.gallery_processor.utils.extractImageMetadata;

import com.drew.imaging.FileType;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import net.furizon.gallery_processor.dto.upload.UploadImageMetadata;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractImageMetadataImpl implements ExtractImageMetadata {
    private static final ZoneId GMT =  ZoneId.of("GMT");

    @Override
    public void parseExif(@NotNull String path, @NotNull GalleryProcessorUploadData resultObj, @NotNull FileType fileType) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(path));

            String cameraMaker = null;
            String cameraModel = null;
            String lensMaker = null;
            String lensModel = null;
            String focal = null;
            String shutter = null;
            String aperture = null;
            String iso = null;
            LocalDateTime shotTime = null;

            var subIfdDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (subIfdDir != null) {
                var subIfdDesc = new ExifSubIFDDescriptor(subIfdDir);

                focal = subIfdDesc.getFocalLengthDescription();
                shutter = subIfdDesc.getShutterSpeedDescription();
                aperture = subIfdDesc.getApertureValueDescription();
                iso = subIfdDesc.getIsoEquivalentDescription();

                TimeZone zone = getTimeZone(subIfdDir);
                ZoneId zoneId = zone == null ? GMT : zone.toZoneId();
                var date = subIfdDir.getDateOriginal();
                if (date != null) {
                    shotTime = LocalDateTime.ofInstant(date.toInstant(), zoneId);
                }

                lensMaker = subIfdDir.getString(ExifIFD0Directory.TAG_LENS_MAKE);
                lensModel = subIfdDir.getString(ExifIFD0Directory.TAG_LENS_MODEL);
            }

            var ifd0Dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0Dir != null) {
                //var ifd0Desc = new ExifIFD0Descriptor(ifd0Dir);

                cameraMaker = ifd0Dir.getString(ExifIFD0Directory.TAG_MAKE);
                cameraModel = ifd0Dir.getString(ExifIFD0Directory.TAG_MODEL);

                lensMaker = lensMaker != null ? lensMaker : ifd0Dir.getString(ExifIFD0Directory.TAG_LENS_MAKE);
                lensModel = lensModel != null ? lensModel : ifd0Dir.getString(ExifIFD0Directory.TAG_LENS_MODEL);

            }

            resultObj.setShotTimestamp(shotTime);
            Integer width = null, height = null;
            var widthMetadata = WIDTH_TAG_MAP.get(fileType);
            var heightMetadata = HEIGHT_TAG_MAP.get(fileType);
            resultObj.setResolutionWidth(widthMetadata == null ? 0 : int_(widthMetadata, metadata));

            if (cameraMaker == null
                    && cameraModel == null
                    && lensMaker == null
                    && lensModel == null
                    && focal == null
                    && shutter == null
                    && aperture == null
                    && iso == null) {
                return;
            }

            var obj = UploadImageMetadata.builder()
                    .cameraMaker(cameraMaker)
                    .cameraModel(cameraModel)
                    .lensMaker(lensMaker)
                    .lensModel(lensModel)
                    .focal(focal)
                    .shutter(shutter)
                    .aperture(aperture)
                    .iso(iso)
                    .build();

            resultObj.setPhotoMetadata(obj);

        } catch (IOException e) {
            log.warn("Error while reading uploaded file");
        } catch (ImageProcessingException e) {
            log.warn("Error while parsing uploaded file's metadata");
        }
    }

    @Nullable
    private TimeZone getTimeZone(ExifSubIFDDirectory directory) {
        String timeOffset = directory.getString(ExifSubIFDDirectory.TAG_TIME_ZONE_ORIGINAL);
        return timeOffset != null && timeOffset.matches("[\\+\\-]\\d\\d:\\d\\d") ? TimeZone.getTimeZone("GMT" + timeOffset) : null;
    }


    // Partially from https://github.com/drewnoakes/metadata-extractor/discussions/691

    private @Nullable <T extends Directory> Integer int_(@NotNull Pair<Class<T>, Integer> tag, @NotNull Metadata metadata) {
        T dir = metadata.getFirstDirectoryOfType(tag.getFirst());
        if (dir == null) {
            return null;
        }
        return dir.getInteger(tag.getSecond());
    }

    private static final Map<FileType, Pair<Class<? extends Directory>, Integer>> HEIGHT_TAG_MAP = Map.ofEntries(
            Map.entry(FileType.Png,  Pair.of(PngDirectory.class,   PngDirectory.TAG_IMAGE_HEIGHT)),
            Map.entry(FileType.Jpeg, Pair.of(JpegDirectory.class, JpegDirectory.TAG_IMAGE_HEIGHT)),
            Map.entry(FileType.WebP, Pair.of(WebpDirectory.class, WebpDirectory.TAG_IMAGE_HEIGHT)),
            Map.entry(FileType.Heif, Pair.of(HeifDirectory.class, HeifDirectory.TAG_IMAGE_HEIGHT)),
            Map.entry(FileType.Tiff, Pair.of(JpegDirectory.class, JpegDirectory.TAG_IMAGE_HEIGHT))
    );

    private static final Map<FileType, Pair<Class<Directory>, Integer>> WIDTH_TAG_MAP = Map.ofEntries(
            Map.entry(FileType.Png,  Pair.of(PngDirectory.class,   PngDirectory.TAG_IMAGE_WIDTH)),
            Map.entry(FileType.Jpeg, Pair.of(JpegDirectory.class, JpegDirectory.TAG_IMAGE_WIDTH)),
            Map.entry(FileType.WebP, Pair.of(WebpDirectory.class, WebpDirectory.TAG_IMAGE_WIDTH)),
            Map.entry(FileType.Heif, Pair.of(HeifDirectory.class, HeifDirectory.TAG_IMAGE_WIDTH)),
            Map.entry(FileType.Tiff, Pair.of(JpegDirectory.class, JpegDirectory.TAG_IMAGE_WIDTH))
    );

}
