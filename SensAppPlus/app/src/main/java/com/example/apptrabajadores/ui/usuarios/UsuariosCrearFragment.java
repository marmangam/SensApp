// Este archivo define el fragmento `UsuariosCrearFragment`.
// Permite a los administrativos crear un nuevo usuario.
// Para esto se incluyen un método para crear un usuario.
// También permite añadir responsables a este usuario una vez creado.

package com.example.apptrabajadores.ui.usuarios;

// Imports
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.apptrabajadores.Usuario;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentCrearUsuariosBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuariosCrearFragment extends Fragment {
    // Variables de la clase
    private FragmentCrearUsuariosBinding binding;
    Activity activity;
    EditText textonombre;
    EditText textoapellidos;
    EditText textodni;
    EditText textotelefono;
    DatePicker date_picker;
    EditText textodomicilio;
    EditText textoenfermedades;
    EditText textoalergias;
    EditText textopassword;
    ImageView ver_contrasena;
    Button boton_crear;
    List <String> dnis_usuarios;
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentCrearUsuariosBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_administrativos);
        activity = getActivity();
        // Obtenemos una lista con todos los dnis de los usuarios existentes para asegurarnos de no crear dos usuarios con el mismo dni
        dnis_usuarios = new ArrayList<>();
        listar_dnis();

        // Configuramos los datos del usuario en pantalla
        textonombre = binding.textNombre;
        textoapellidos = binding.textApellidos;
        textodni = binding.textDni;
        textotelefono = binding.textTelefono;
        date_picker = binding.datePickerNac;
        date_picker.updateDate(1930, 0, 1);
        textodomicilio = binding.textdomicilio;
        textoenfermedades = binding.textenfermedades;
        textoalergias = binding.textalergias;
        textopassword = binding.textPassword;
        // Configuramos la funcionalidad para ver la contraseña al pulsar la imagen
        ver_contrasena = binding.imagenVerPassword;
        ver_contrasena.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                switch (evento.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        textopassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        textopassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        return true;
                }
                return false;
            }
        });

        // Configuramos el botón crear usuario
        boton_crear = binding.botonCrearUsuario;
        boton_crear.setOnClickListener(v ->{
            // Funcionamiento del botón:
            // Obtenemos los datos del usuario a crear
            String nuevonombre =    binding.textNombre.getText().toString();
            String nuevoapellido = binding.textApellidos.getText().toString();
            String nuevodni = binding.textDni.getText().toString();
            String textotelefono = binding.textTelefono.getText().toString();
            LocalDate nuevafecha = LocalDate.of(binding.datePickerNac.getYear(), binding.datePickerNac.getMonth()+1, binding.datePickerNac.getDayOfMonth());
            String nuevodomicilio = binding.textdomicilio.getText().toString();
            String nuevaenfermedad = binding.textenfermedades.getText().toString();
            String nuevaalergia= binding.textalergias.getText().toString();
            String nuevapassword = binding.textPassword.getText().toString();
            // Comprobamos que no exista un usuario con el dni introducido
            if(dnis_usuarios.contains(nuevodni)){
                Toast.makeText(activity.getApplicationContext(), "Ya existe un usuario con el dni introducido", Toast.LENGTH_SHORT).show();
            }// Comprobamos que no estén vacíos y actualizamos los datos si no lo están
            else if (nuevonombre.equals("") || nuevoapellido.equals("") || nuevodni.equals("")
                    || textotelefono.isEmpty() || nuevodomicilio.equals("") || nuevapassword.equals("")) {
                Toast.makeText(activity.getApplicationContext(), "Rellene todos los campos!", Toast.LENGTH_SHORT).show();
            }else {
                Integer nuevotelefono = Integer.valueOf(binding.textTelefono.getText().toString());
                crear_usuario(nuevonombre, nuevoapellido, nuevodni,
                        nuevotelefono, nuevafecha, nuevodomicilio, nuevaenfermedad, nuevaalergia, nuevapassword);
            }

        });



        return view;
    }


    // Método para crear un nuevo usuario, enviando la solicitud al servidor
    public void crear_usuario(String nombre, String apellidos, String dni, Integer telefono, LocalDate fecha, String domicilio, String enfermedades, String alergias, String password){
        // Creamos el JSON que vamos a enviar
        JSONObject new_usuario= new JSONObject();
        try {
            new_usuario.put("dni", dni);
            new_usuario.put("nombre", nombre);
            new_usuario.put("apellidos", apellidos);
            new_usuario.put("telefono", telefono);
            new_usuario.put("fecha_nacimiento", fecha);
            new_usuario.put("domicilio", domicilio);
            new_usuario.put("enfermedades_previas", enfermedades);
            new_usuario.put("alergias", alergias);
            new_usuario.put("contrasena",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"usuario";

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, new_usuario,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Insertado")) {
                                // Toast.makeText(activity.getApplicationContext(), "Usuario insertado", Toast.LENGTH_SHORT).show();
                                // En caso de que el usuario se cree correctamente, se  pregunta si se desea añadir un responsable
                                // y en caso de que si se navega al fragment ResponsablesAddFragment pasando el dni del usuario creado en el bundle
                                Usuario nuevousuario= new Usuario(nombre, apellidos, fecha, domicilio, enfermedades, alergias, dni, telefono, password);

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                TextView titulo = new TextView(getContext());
                                titulo.setText("Añadir responsables");
                                titulo.setTextSize(30);
                                titulo.setPadding(25, 25, 25, 25);
                                titulo.setTypeface(null, Typeface.BOLD);
                                titulo.setGravity(Gravity.CENTER);
                                builder.setCustomTitle(titulo);

                                TextView mensaje = new TextView(getContext());
                                mensaje.setText("¿Desea añadir un responsable?");
                                mensaje.setTextSize(20);
                                mensaje.setPadding(25, 25, 25, 25);
                                mensaje.setGravity(Gravity.CENTER);
                                builder.setView(mensaje);
                                builder.setCancelable(false)
                                        .setPositiveButton("Sí", (dialog, which) -> {
                                            Bundle bundle = new Bundle();
                                            bundle.putString("dni_usuario", dni);
                                            Log.d("UsuariosFragment", "JSON del usuario: " + dni);
                                            navController.popBackStack();
                                            navController.navigate(R.id.nav_add_responsables, bundle);
                                        })
                                        // En caso de que no se quiera añadir a un responsable se navega a UsuariosFragment
                                        .setNegativeButton("No", (dialog, which) -> {
                                            navController.popBackStack();
                                            navController.navigate(R.id.nav_usuarios);
                                        });
                                AlertDialog dialogo = builder.create();
                                dialogo.show();

                                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
                                dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(20);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de error se imprime un Toast por pantalla para informar al usuario
                VolleyLog.e("Error: ", error.getMessage());
                Toast.makeText(activity.getApplicationContext(),
                        "Se ha producido un error creando el usuario", Toast.LENGTH_SHORT).show();
                error.printStackTrace();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        ServiciosWeb.getInstance().getRequestQueue().add(request);

       // return resultado;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    // Método para obtener los dnis de todos los usuarios existentes, enviando la solicitud al servidor
    public void listar_dnis(){
        final String URL = getString(R.string.IP) + "usuario/lista";

        // Mandamos la solicitud
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Se añaden todos los dnis de los usuarios a una lista
                        try {
                            dnis_usuarios.clear();
                            for (int i =0; i<response.length(); i++){
                                JSONObject usuario = response.getJSONObject(i);
                                String dni = usuario.getString("dni");
                                dnis_usuarios.add(dni);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                error.printStackTrace();
            }
        });

        ServiciosWeb.getInstance().getRequestQueue().add(request);
    }

}