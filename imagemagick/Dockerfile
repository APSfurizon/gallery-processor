FROM alpine:latest

RUN apk add git build-base autoconf automake libtool pkgconf \
  libc++-static libpng-dev libpng-static libjpeg-turbo \
  jpeg-dev libjpeg-turbo-dev libjpeg-turbo-static zlib-static libxml2 \
  libxml2-dev libxml2-static xz-static upx \
  libpng-dev \
  libjpeg-turbo-dev \
  tiff-dev \
  libwebp-dev \
  lcms2-dev \
  libheif-dev \
  openexr-dev \
  openjpeg-dev \
  libraw-dev \
  librsvg-dev \
  pango-dev \
  fftw-dev \
#  liblqr-dev \
  libxml2-dev \
  bzip2-dev \
  zlib-dev \
  xz-dev \
  fontconfig-dev \
  freetype-dev \
  ghostscript-dev \
  djvulibre-dev \
  graphviz-dev \
  libfpx libfpx-dev

RUN git clone --depth 1 --branch 7.1.2-21 https://github.com/ImageMagick/ImageMagick.git ImageMagick

WORKDIR ImageMagick

ENV LDFLAGS="-static"
ENV CFLAGS="--static -Wl,-static"
ENV CXXFLAGS="--static -Wl,-static"

RUN ./configure --disable-shared --enable-static \
  --with-magick-plus-plus=no --with-modules --enable-hdri=yes \
  --with-bzlib=yes --with-autotrace=yes --with-djvu=yes --with-dps=yes --with-fftw=yes --with-flif=yes --with-fpx=yes --with-fontconfig=yes --with-freetype=yes --with-gslib=yes --with-gvc=yes --with-heic=yes --with-jbig=yes --with-jpeg=yes --with-jxl=yes --with-dmr=yes --with-lcms=yes --with-lqr=yes --with-ltdl=yes --with-lzma=yes --with-magick-plus-plus=yes --with-openexr=yes --with-openjp2=yes --with-pango=yes --with-perl=yes --with-png=yes --with-raqm=yes --with-raw=yes --with-rsvg=yes --with-tiff=yes --with-uhdr=yes --with-webp=yes --with-wmf=yes --with-xml=yes --with-zip=yes --with-zlib=yes --with-zstd=yes

RUN make -j"$(nproc)"

RUN strip -s -R .comment -R .gnu.version --strip-unneeded utilities/magick

RUN upx --best --lzma utilities/magick

