package se.medituner.app;

import android.content.res.Resources;

public enum Medication {

    AEROBEC,
    AEROBECAUTOHALER,
    AEROBECSPRAY,
    AERVIROSPIROMAX,
    AIRFLUSALFORSPIRO,
    AIROBECAUTOHALER,
    AIROMIR,
    AIROMIRAUTOHALER,
    AIRSALB,
    ALVESCO,
    ANOROELLIPTA,
    ASMANEXTWISTHALER,
    ATROVENT,
    BECLOMETMEDIUM,
    BRALTUSZONDA,
    BRICANYLTURBOHALER,
    BUDESONIDMEDIUM,
    BUFOMIXMEDIUM,
    BUVENTOLMEDIUM,
    CIPLAFLUTICASONE,
    CIPLAFLUTISALM,
    DUKALIRGENUAIR,
    DUORESPSPIROMAX,
    EKLIRAGENUAIR,
    EYEDROP,
    FLUTIDE,
    FLUTIDEDISKUSORANGE,
    FLUTIFORM,
    FORMATRISNOVOLIZER,
    HANDIHALER,
    INCRUSEELLIPTA,
    INNOVAIR,
    MISSING,
    NOSESPRAY,
    NOVOPULMONNOVOLIZER,
    ONBREZBREEZHALER,
    OXISTURBOHALER,
    PILL,
    PULMICORTTURBOHALER,
    RELANIO,
    RELVAR,
    SALMETEROLFLUTICASONECIPLA,
    SEEBRIBREEZHALER,
    SERETIDE,
    SERETIDEDISKUSLILA,
    SEREVENTDISKUS,
    SEREVENTEVOHALER,
    SEREVENTEVOHALEORANGE,
    SPIOLTORESPIMAT,
    SPIRIVARESPIMAT,
    STRIVERDIRESPIMAT,
    SYMBICORT,
    ULTIBROBREEZEHALER,
    VENTASTINNOVOLIZER,
    VENTOLINEDISKUS,
    VENTOLINEEVOHALERBLUE;

    public static int getImageId(Resources res, String packageName, Medication medication) {
        return res.getIdentifier(medication.name().toLowerCase(), "drawable", packageName);
    }

    public static String getName(Resources res, String packageName, Medication medication) {
        return res.getString(res.getIdentifier(medication.name().toLowerCase(), "string", packageName));
    }
}
