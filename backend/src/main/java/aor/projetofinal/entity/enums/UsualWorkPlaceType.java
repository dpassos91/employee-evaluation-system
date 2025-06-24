package aor.projetofinal.entity.enums;

public enum UsualWorkPlaceType {
BOSTON,
COIMBRA,
LISBOA,
MUNICH,
PORTO,
SOUTHAMPTON,
VISEU;

    public static UsualWorkPlaceType fromString(String local) throws IllegalArgumentException {
        if (local == null) throw new IllegalArgumentException("Workplace cannot be null");
        return UsualWorkPlaceType.valueOf(local.toUpperCase()); // Ex: "lisboa" → "LISBOA"
    }

    public static String transformToString(UsualWorkPlaceType local) {
        if (local == null) throw new IllegalArgumentException("UsualWorkPlaceType cannot be null.");
        return local.name();
    }



    }
    
