// Esta clase gestiona la bbdd sqlite local de la aplicación.
// Proporciona métodos para crear y consultar elementos de su única tabla: usuarios.

package com.example.appusuarios;

// Imports
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;


public class BaseDatos extends SQLiteOpenHelper {


    private static final int VERSION_BASEDATOS = 1;
    private static final String NOMBRE_BASEDATOS = "basedatos.db";
    private static final String TABLA_USUARIOS ="CREATE TABLE IF NOT EXISTS usuarios " +
            " (dni TEXT PRIMARY KEY, contrasena TEXT)";

    // Constructor de la bbdd
    public BaseDatos(Context context) {
        super(context, NOMBRE_BASEDATOS, null, VERSION_BASEDATOS);
    }

    // Crea la tabla USUARIOS
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLA_USUARIOS);
    }

    // Actualiza la bbdd cuando se cambia de versión
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLA_USUARIOS);
        onCreate(db);
    }


    // Método para insertar un nuevo usuario en la tabla USUARIOS de la bbdd
    public boolean insertarUSUARIO(String dni, String contrasena) {
        long salida=0;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query("usuarios", new String[]{"dni"}, "dni = ?", new String[]{dni}, null, null, null);
        boolean yaexiste = cursor.getCount() > 0;
        if (!yaexiste) {
            ContentValues valores = new ContentValues();
            valores.put("dni", dni);
            valores.put("contrasena", contrasena);
            salida=db.insert("usuarios", null, valores);
        }
        db.close();
        cursor.close();
        return(salida>0);
    }

    // Método que devuelve una lista con todos los usuarios almacenados en la tabla USUARIOS
    public ArrayList<Usuario> recuperarUSUARIOS() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Usuario> lista_usuarios = new ArrayList<Usuario>();
        String[] valores_recuperar = {"dni", "contrasena"};
        Cursor c = db.query("usuarios", valores_recuperar, null, null, null, null, null, null);
        if (c !=null && c.moveToFirst()) {
            do {
                Usuario usuario = new Usuario(c.getString(0), c.getString(1));
                lista_usuarios.add(usuario);
            } while (c.moveToNext());
        }
        db.close();
        c.close();
        return lista_usuarios;
    }



}

