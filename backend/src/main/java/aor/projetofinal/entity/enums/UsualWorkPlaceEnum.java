package aor.projetofinal.entity.enums;

public enum UsualWorkPlaceEnum {
BOSTON,
COIMBRA,
LISBOA,
MUNICH,
PORTO,
SOUTHAMPTON,
VISEU;

    public static UsualWorkPlaceEnum fromString(String local) throws IllegalArgumentException {
        if (local == null) throw new IllegalArgumentException("Workplace cannot be null");
        return UsualWorkPlaceEnum.valueOf(local.toUpperCase()); // Ex: "lisboa" → "LISBOA"
    }

    public static String transformToString(UsualWorkPlaceEnum local) {
        if (local == null) throw new IllegalArgumentException("UsualWorkPlaceType cannot be null.");
        return local.name();
    }



    }
    
