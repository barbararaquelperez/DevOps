package es.usj.androidapps.model.dto

data class ActorDTO(var id: Long = 0, var name: String = "") {
    init {
        name = name.trim()
    }
}