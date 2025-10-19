package de.sfuhrm.imagemagick.spi;

enum ColorspaceType {
    UndefinedColorspace,
    CMYColorspace,           /* negated linear RGB colorspace */
    CMYKColorspace,          /* CMY with Black separation */
    GRAYColorspace,          /* Single Channel greyscale (non-linear) image */
    HCLColorspace,
    HCLpColorspace,
    HSBColorspace,
    HSIColorspace,
    HSLColorspace,
    HSVColorspace,           /* alias for HSB */
    HWBColorspace,
    LabColorspace,
    LCHColorspace,           /* alias for LCHuv */
    LCHabColorspace,         /* Cylindrical (Polar) Lab */
    LCHuvColorspace,         /* Cylindrical (Polar) Luv */
    LogColorspace,
    LMSColorspace,
    LuvColorspace,
    OHTAColorspace,
    Rec601YCbCrColorspace,
    Rec709YCbCrColorspace,
    RGBColorspace,           /* Linear RGB colorspace */
    scRGBColorspace,         /* ??? */
    sRGBColorspace,          /* Default: non-linear sRGB colorspace */
    TransparentColorspace,
    xyYColorspace,
    XYZColorspace,           /* IEEE Color Reference colorspace */
    YCbCrColorspace,
    YCCColorspace,
    YDbDrColorspace,
    YIQColorspace,
    YPbPrColorspace,
    YUVColorspace,
    LinearGRAYColorspace,     /* Single Channel greyscale (linear) image */
    JzazbzColorspace,
    DisplayP3Colorspace,
    Adobe98Colorspace,
    ProPhotoColorspace,
    OklabColorspace,
    OklchColorspace;

    /** Get the channel count for this color space type.
     * */
    int getChannelCount() {
        int channelCount = 0;

        switch (this) {
            case RGBColorspace:
            case HSLColorspace:
            case HSVColorspace:
            case HWBColorspace:
                // Rot, Grün, Blau / Hue, Saturation, Lightness/Value/Brightness
                channelCount = 3;
                break;
            case CMYKColorspace:
                // Cyan, Magenta, Gelb, Schwarz
                channelCount = 4;
                break;
            case GRAYColorspace:
            case UndefinedColorspace:
                // Grauwerte (Graustufe)
                channelCount = 1;
                break;
            // Fügen Sie weitere Farbräume hinzu, die Sie behandeln müssen
            default:
                // Schätzen Sie auf 3, wenn unbekannt, oder verwenden Sie eine sicherere Methode
                channelCount = 3;
                break;
        }
        return channelCount;
    }
}