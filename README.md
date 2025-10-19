Magick2ImageIO Java Library
===========================

Reading and writing more than 250 image formats using the
famous ImageMagick library, enabling most existing Java applications.
The library is using a Foreign Function and Memory API adapter towards the
ImageMagick C library.

## Installation / Preperations

You need to run Java 22+ for the Foreign Function and Memory API to exist.

You need to have `libmagickwand-7` installed on your system. In
the following sections I'll describe how to install it on different
systems.

### Debian

```
# apt-get install libmagickwand-7.q16-10
```

### Ubuntu

```declarative
# apt-get install libmagickwand-6.q16-7t64
```

## Supported image formats (via ImageMagick)

The following is the list of supported formats by ImageMagick:

* AAI
* AI
* APNG
* ART
* ARW
* ASHLAR
* AVCI
* AVI
* AVIF
* AVS
* BAYER
* BAYERA
* BGR
* BGRA
* BGRO
* BIE
* BMP
* BMP2
* BMP3
* BRF
* CAL
* CALS
* CANVAS
* CAPTION
* CIN
* CIP
* CLIP
* CMYK
* CMYKA
* CR2
* CR3
* CRW
* CUBE
* CUR
* CUT
* DATA
* DCM
* DCR
* DCRAW
* DCX
* DDS
* DFONT
* DJVU
* DNG
* DOT
* DPX
* DXT1
* DXT5
* EPDF
* EPI
* EPS
* EPS2
* EPS3
* EPSF
* EPSI
* EPT
* EPT2
* EPT3
* ERF
* EXR
* FARBFELD
* FAX
* FF
* FFF
* FILE
* FITS
* FL32
* FLV
* FRACTAL
* FTP
* FTS
* FTXT
* G3
* G4
* GIF
* GIF87
* GRADIENT
* GRAY
* GRAYA
* GROUP4
* GV
* HALD
* HDR
* HEIC
* HEIF
* HISTOGRAM
* HRZ
* HTM
* HTML
* HTTP
* HTTPS
* ICB
* ICO
* ICON
* IIQ
* INFO
* INLINE
* IPL
* ISOBRL
* ISOBRL6
* J2C
* J2K
* JBG
* JBIG
* JNG
* JNX
* JP2
* JPC
* JPE
* JPEG
* JPG
* JPM
* JPS
* JPT
* JSON
* K25
* KDC
* KERNEL
* LABEL
* M2V
* M4V
* MAC
* MAP
* MASK
* MAT
* MATTE
* MDC
* MEF
* MIFF
* MKV
* MNG
* MONO
* MOS
* MOV
* MP4
* MPC
* MPEG
* MPG
* MPO
* MRW
* MSL
* MSVG
* MTV
* MVG
* NEF
* NRW
* NULL
* ORA
* ORF
* OTB
* OTF
* PAL
* PALM
* PAM
* PANGO
* PATTERN
* PBM
* PCD
* PCDS
* PCL
* PCT
* PCX
* PDB
* PDF
* PDFA
* PEF
* PES
* PFA
* PFB
* PFM
* PGM
* PGX
* PHM
* PICON
* PICT
* PIX
* PJPEG
* PLASMA
* PNG
* PNG00
* PNG24
* PNG32
* PNG48
* PNG64
* PNG8
* PNM
* POCKETMOD
* PPM
* PS
* PS2
* PS3
* PSB
* PSD
* PTIF
* PWP
* QOI
* RAF
* RAS
* RAW
* RGB
* RGB565
* RGBA
* RGBO
* RGF
* RLA
* RLE
* RMF
* RW2
* RWL
* SCR
* SCT
* SFW
* SGI
* SHTML
* SIX
* SIXEL
* SR2
* SRF
* SRW
* STEGANO
* STI
* STRIMG
* SUN
* SVG
* SVGZ
* TEXT
* TGA
* THUMBNAIL
* TIFF
* TIFF64
* TILE
* TIM
* TM2
* TTC
* TTF
* TXT
* UBRL
* UBRL6
* UIL
* UYVY
* VDA
* VICAR
* VID
* VIFF
* VIPS
* VST
* WBMP
* WEBM
* WEBP
* WMF
* WMV
* WMZ
* WPG
* X
* X3F
* XBM
* XC
* XCF
* XPM
* XPS
* XV
* XWD
* YAML
* YCBCR
* YCBCRA
* YUV

## Restrictions

At the moment the following restrictions are in effect:

* There are no descriptions on how to install on platforms other than Debian.
* Only 8 bit and 16 bit images are supported.
* No Alpha channel support for grayscale images.
* No support for meta data.
* No setting of the compression level / quality of codecs (JPEG, AVIF, etc.).
