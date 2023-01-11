package exception

class ArtifactParsingException(override val message: String?, override val cause: Throwable?) :
    RuntimeException(message, cause)
