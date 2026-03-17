package net.furizon.gallery_processor.utils.extractImageMetadata;

import com.drew.imaging.FileType;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.avi.AviDirectory;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.eps.EpsDirectory;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifImageDirectory;
import com.drew.metadata.exif.ExifInteropDirectory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.exif.makernotes.CanonMakernoteDirectory;
import com.drew.metadata.exif.makernotes.KodakMakernoteDirectory;
import com.drew.metadata.exif.makernotes.OlympusMakernoteDirectory;
import com.drew.metadata.exif.makernotes.PanasonicMakernoteDirectory;
import com.drew.metadata.gif.GifImageDirectory;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.ico.IcoDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import net.furizon.gallery_processor.dto.upload.UploadImageMetadata;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractImageMetadataImpl implements ExtractImageMetadata {
    private static final ZoneId GMT =  ZoneId.of("GMT");

    @Override
    public void parseExif(@NotNull Path file, @NotNull GalleryProcessorUploadData resultObj, @NotNull FileType fileType) {
        try {
            log.info("Parsing metadata from {}", file);
            Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());

            for (Directory directory : metadata.getDirectories())
                for (Tag tag : directory.getTags())
                    log.info("[{}] {} {}: {}", tag.getDirectoryName(), tag.getTagTypeHex(), tag.getTagName(), tag.getDescription());

            String cameraMaker = null;
            String cameraModel = null;
            String lensMaker = null;
            String lensModel = null;
            String focal = null;
            String shutter = null;
            String aperture = null;
            String iso = null;
            OffsetDateTime shotTime = null;

            var subIfdDirList = metadata.getDirectoriesOfType(ExifSubIFDDirectory.class);
            for (var subIfdDir : subIfdDirList) {
                var subIfdDesc = new ExifSubIFDDescriptor(subIfdDir);

                if (focal == null) focal = subIfdDesc.getFocalLengthDescription();
                if (shutter == null) shutter = subIfdDesc.getShutterSpeedDescription();
                if (shutter == null) shutter = subIfdDesc.getExposureTimeDescription();
                if (aperture == null) aperture = subIfdDesc.getApertureValueDescription();
                if (aperture == null) aperture = subIfdDesc.getFNumberDescription();
                if (iso == null) iso = subIfdDesc.getIsoEquivalentDescription();

                if (shotTime == null) {
                    TimeZone zone = getTimeZone(subIfdDir);
                    ZoneId zoneId = zone == null ? GMT : zone.toZoneId();
                    var date = subIfdDir.getDateOriginal();
                    if (date != null) {
                        shotTime = OffsetDateTime.ofInstant(date.toInstant(), zoneId);
                    }
                }

                if (lensMaker == null) lensMaker = subIfdDir.getString(ExifIFD0Directory.TAG_LENS_MAKE);
                if (lensModel == null) lensModel = subIfdDir.getString(ExifIFD0Directory.TAG_LENS_MODEL);
            }

            var ifd0DirList = metadata.getDirectoriesOfType(ExifIFD0Directory.class);
            for (var ifd0Dir : ifd0DirList) {
                //var ifd0Desc = new ExifIFD0Descriptor(ifd0Dir);

                if (cameraMaker == null) cameraMaker = ifd0Dir.getString(ExifIFD0Directory.TAG_MAKE);
                if (cameraModel == null) cameraModel = ifd0Dir.getString(ExifIFD0Directory.TAG_MODEL);

                if (lensMaker == null) lensMaker = ifd0Dir.getString(ExifIFD0Directory.TAG_LENS_MAKE);
                if (lensModel == null) lensModel = ifd0Dir.getString(ExifIFD0Directory.TAG_LENS_MODEL);

            }

            if (resultObj.getShotTimestamp() == null) resultObj.setShotTimestamp(shotTime);
            //This may still fail, so an extra call to imagemagick might be needed
            if (resultObj.getResolutionWidth() == 0) {
                Integer i = getImageWidth(metadata);
                if (i != null) resultObj.setResolutionWidth(i);
            }
            if (resultObj.getResolutionHeight() == 0) {
                Integer i = getImageHeight(metadata);
                if (i != null) resultObj.setResolutionHeight(i);
            }

            if (cameraMaker == null
                    && cameraModel == null
                    && lensMaker == null
                    && lensModel == null
                    && focal == null
                    && shutter == null
                    && aperture == null
                    && iso == null) {
                log.warn("Early return from {} since all params were null", file);
                return;
            }

            if (resultObj.getPhotoMetadata() != null) {
                UploadImageMetadata data =  resultObj.getPhotoMetadata();
                if (data.getCameraMaker() == null) data.setCameraMaker(cameraMaker);
                if (data.getCameraModel() == null) data.setCameraModel(cameraModel);
                if (data.getLensMaker() == null) data.setLensMaker(lensMaker);
                if (data.getLensModel() == null) data.setLensModel(lensModel);
                if (data.getFocal() == null) data.setFocal(focal);
                if (data.getShutter() == null) data.setShutter(shutter);
                if (data.getAperture() == null) data.setAperture(aperture);
                if (data.getIso() == null) data.setIso(iso);
            } else {
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
            }

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

    private @Nullable <T extends Directory> Integer int_(@NotNull Class<T> dirClass, int tagType, @NotNull Metadata metadata) {
        T dir = metadata.getFirstDirectoryOfType(dirClass);
        if (dir == null) {
            return null;
        }
        return dir.getInteger(tagType);
    }

    private <T> T coalesce(Supplier<T>... suppliers) {
        for (Supplier<T> supplier : suppliers) {
            T answer = supplier.get();
            if (answer != null) {
                return answer;
            }
        }
        return null;
    }

    public Integer getImageHeight(@NotNull Metadata metadata) {
        return coalesce(
                () -> int_(JpegDirectory.class, JpegDirectory.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(PngDirectory.class, PngDirectory.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(AviDirectory.class, AviDirectory.TAG_HEIGHT, metadata),
                () -> int_(BmpHeaderDirectory.class, BmpHeaderDirectory.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(EpsDirectory.class, EpsDirectory.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT, metadata),
                () -> int_(CanonMakernoteDirectory.class, CanonMakernoteDirectory.AFInfo.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(ExifIFD0Directory.class, ExifDirectoryBase.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(ExifImageDirectory.class, ExifDirectoryBase.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(ExifInteropDirectory.class, ExifDirectoryBase.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(GpsDirectory.class, ExifDirectoryBase.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(GifImageDirectory.class, GifImageDirectory.TAG_HEIGHT, metadata),
                () -> int_(HeifDirectory.class, HeifDirectory.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(IcoDirectory.class, IcoDirectory.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(KodakMakernoteDirectory.class, KodakMakernoteDirectory.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(OlympusMakernoteDirectory.class, OlympusMakernoteDirectory.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(PanasonicMakernoteDirectory.class, PanasonicMakernoteDirectory.TAG_PANASONIC_IMAGE_HEIGHT, metadata),
                () -> int_(WebpDirectory.class, WebpDirectory.TAG_IMAGE_HEIGHT, metadata),
                () -> int_(QuickTimeVideoDirectory.class, QuickTimeVideoDirectory.TAG_HEIGHT, metadata)
        );
    }

    public Integer getImageWidth(@NotNull Metadata metadata) {
        return coalesce(
                () -> int_(JpegDirectory.class, JpegDirectory.TAG_IMAGE_WIDTH, metadata),
                () -> int_(PngDirectory.class, PngDirectory.TAG_IMAGE_WIDTH, metadata),
                () -> int_(AviDirectory.class, AviDirectory.TAG_WIDTH, metadata),
                () -> int_(BmpHeaderDirectory.class, BmpHeaderDirectory.TAG_IMAGE_WIDTH, metadata),
                () -> int_(EpsDirectory.class, EpsDirectory.TAG_IMAGE_WIDTH, metadata),
                () -> int_(ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH, metadata),
                () -> int_(CanonMakernoteDirectory.class, CanonMakernoteDirectory.AFInfo.TAG_IMAGE_WIDTH, metadata),
                () -> int_(ExifIFD0Directory.class, ExifDirectoryBase.TAG_IMAGE_WIDTH, metadata),
                () -> int_(ExifImageDirectory.class, ExifDirectoryBase.TAG_IMAGE_WIDTH, metadata),
                () -> int_(ExifInteropDirectory.class, ExifDirectoryBase.TAG_IMAGE_WIDTH, metadata),
                () -> int_(GpsDirectory.class, ExifDirectoryBase.TAG_IMAGE_WIDTH, metadata),
                () -> int_(GifImageDirectory.class, GifImageDirectory.TAG_WIDTH, metadata),
                () -> int_(HeifDirectory.class, HeifDirectory.TAG_IMAGE_WIDTH, metadata),
                () -> int_(IcoDirectory.class, IcoDirectory.TAG_IMAGE_WIDTH, metadata),
                () -> int_(KodakMakernoteDirectory.class, KodakMakernoteDirectory.TAG_IMAGE_WIDTH, metadata),
                () -> int_(OlympusMakernoteDirectory.class, OlympusMakernoteDirectory.TAG_IMAGE_WIDTH, metadata),
                () -> int_(PanasonicMakernoteDirectory.class, PanasonicMakernoteDirectory.TAG_PANASONIC_IMAGE_WIDTH, metadata),
                () -> int_(WebpDirectory.class, WebpDirectory.TAG_IMAGE_WIDTH, metadata),
                () -> int_(QuickTimeVideoDirectory.class, QuickTimeVideoDirectory.TAG_WIDTH, metadata)
        );
    }

}
