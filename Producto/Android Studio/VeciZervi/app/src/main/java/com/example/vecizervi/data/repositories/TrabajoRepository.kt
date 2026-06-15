package com.example.vecizervi.data.repositories

import com.example.vecizervi.data.models.Trabajo
import com.example.vecizervi.data.models.Postulacion
import com.example.vecizervi.data.utils.ApiClient

class TrabajoRepository {
    suspend fun obtenerTrabajos(): List<Trabajo> {
        return try {
            val response = ApiClient.servicioTrabajos.getTrabajosDisponibles()
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerTrabajoPorId(idTrabajo: Long): Trabajo? {
        return try {
            val response = ApiClient.servicioTrabajos.getTrabajoPorId(idTrabajo)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun obtenerTrabajosPorCliente(idCliente: Long): List<Trabajo> {
        return try {
            val response = ApiClient.servicioTrabajos.getTrabajosPorCliente(idCliente)
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun publicarTrabajo(
        nuevoTrabajo: Trabajo,
        idCliente: Long,
        idCategoria: Long
    ): Trabajo? {
        return try {
            val response = ApiClient.servicioTrabajos.postTrabajo(
                nuevoTrabajo,   // ← sin nombres
                idCliente,
                idCategoria
            )
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun actualizarTrabajo(idTrabajo: Long, trabajo: Trabajo): Trabajo? {
        return try {
            val response = ApiClient.servicioTrabajos.actualizarTrabajo(idTrabajo, trabajo)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun eliminarTrabajo(idTrabajo: Long): Boolean {
        return try {
            val response = ApiClient.servicioTrabajos.eliminarTrabajo(idTrabajo)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun postularTrabajo(idTrabajo: Long, idTrabajador: Long, mensaje: String): Boolean {
        return try {
            val postulacion = Postulacion(
                idTrabajo = idTrabajo,
                idTrabajador = idTrabajador,
                mensajePresentacion = mensaje
            )
            val response = ApiClient.servicioTrabajos.postularTrabajo(postulacion)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerPostulaciones(idTrabajo: Long): List<Postulacion> {
        return try {
            val response = ApiClient.servicioTrabajos.getPostulaciones(idTrabajo)
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
