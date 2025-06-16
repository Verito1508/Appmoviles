package com.example.planifica.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.example.planifica.model.Contacto

/**
 * Clase utilitaria para acceder a los contactos del dispositivo.
 */
object ContactoUtil {
    
    /**
     * Obtiene una lista de todos los contactos del dispositivo.
     * @param context El contexto de la aplicación.
     * @return Lista de contactos.
     */
    fun obtenerContactos(context: Context): List<Contacto> {
        val contactos = mutableListOf<Contacto>()
        val contentResolver: ContentResolver = context.contentResolver
        
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        
        cursor?.use {
            if (it.count > 0) {
                while (it.moveToNext()) {
                    val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val nombre = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    val tieneNumeroTelefono = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    
                    var telefono: String? = null
                    var email: String? = null
                    
                    // Obtener número de teléfono
                    if (tieneNumeroTelefono > 0) {
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        
                        phoneCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                telefono = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            }
                        }
                    }
                    
                    // Obtener email
                    val emailCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    
                    emailCursor?.use { ec ->
                        if (ec.moveToFirst()) {
                            email = ec.getString(ec.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                        }
                    }
                    
                    contactos.add(Contacto(id, nombre, telefono, email))
                }
            }
        }
        
        return contactos
    }
    
    /**
     * Obtiene un contacto por su ID.
     * @param context El contexto de la aplicación.
     * @param contactId El ID del contacto.
     * @return El contacto encontrado o null si no existe.
     */
    fun obtenerContactoPorId(context: Context, contactId: String): Contacto? {
        val contentResolver: ContentResolver = context.contentResolver
        
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            ContactsContract.Contacts._ID + " = ?",
            arrayOf(contactId),
            null
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val nombre = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val tieneNumeroTelefono = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                
                var telefono: String? = null
                var email: String? = null
                
                // Obtener número de teléfono
                if (tieneNumeroTelefono > 0) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    
                    phoneCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            telefono = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        }
                    }
                }
                
                // Obtener email
                val emailCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    arrayOf(id),
                    null
                )
                
                emailCursor?.use { ec ->
                    if (ec.moveToFirst()) {
                        email = ec.getString(ec.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                    }
                }
                
                return Contacto(id, nombre, telefono, email)
            }
        }
        
        return null
    }
}
