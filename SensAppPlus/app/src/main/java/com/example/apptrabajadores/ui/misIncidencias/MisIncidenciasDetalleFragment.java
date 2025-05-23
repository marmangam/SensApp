// Este archivo define el fragmento `MiIncidenciasDetalleFragment`.
// Permite a los operadores consultar los detalles de una de sus incidencias.

package com.example.apptrabajadores.ui.misIncidencias;

// Imports
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.apptrabajadores.Incidencia;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentDetalleIncidenciasBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.LocalTime;

public class MisIncidenciasDetalleFragment extends Fragment {
    // Variables de la clase
    Activity activity;
    Intent intent;
    String stringjson;
    String stringfecha;
    String stringhora;
    JSONObject IncidenciaObject;
    String dni_usuario;
    long id;
    String operador;
    String descripcion;
    String procedimiento;
    Boolean resuelta;
    Incidencia incidencia;
    String nombre_usuario;
    String apellidos_usuario;
    private NavController navController;
    TextView textonombre;
    private FragmentDetalleIncidenciasBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentDetalleIncidenciasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_operadores);
    // Obtenemos la actividad y los datos del Bundle (incidencia a consultar)
        activity = getActivity();
        Bundle bundle = getArguments();
        if (activity !=null) {
            intent = activity.getIntent();
            if(bundle!=null) {
                stringjson = bundle.getString("incidenciaObject");
                stringhora = bundle.getString("horastring");

                Log.d("IncidenciasFragment", "stringjson: " + stringjson);

                try {
                    IncidenciaObject = new JSONObject(stringjson);
                    id = IncidenciaObject.getLong("id");
                    dni_usuario = IncidenciaObject.getString("dni_usuario");
                    operador = IncidenciaObject.getString("operador");
                    descripcion = IncidenciaObject.getString("descripcion");
                    procedimiento = IncidenciaObject.getString("procedimiento");
                    resuelta = IncidenciaObject.getBoolean("resuelta");
                    stringfecha = IncidenciaObject.getString("fecha");
                    incidencia = new Incidencia(id, dni_usuario, operador, descripcion, procedimiento, resuelta,LocalDate.parse(stringfecha), LocalTime.parse(stringhora));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                Log.e("MiIncidenciasDetalleFragment", "El string JSON es null");
            }
        }
        // Configuramos los datos de la incidencia en pantalla
        textonombre = binding.textNombreUsuario;
        TextView textdni = binding.textDNIUsuario;
        textdni.setText(dni_usuario);
        consultar_usuario();

        if(incidencia.getResuelta()){
            binding.linearLayoutId.setBackgroundResource(R.color.resuelta);
        }else{
            binding.linearLayoutId.setBackgroundResource(R.color.noresuelta);
        }
        TextView textoid= binding.textId;
        textoid.setText(textoid.getText().toString() + String.valueOf(incidencia.getId()));
        TextView textofecha = binding.textFecha;
        textofecha.setText(incidencia.getFecha().toString());
        TextView textohora = binding.textHora;
        textohora.setText(incidencia.getHora().toString());
        TextView textodesc = binding.textDescripcion;
        String descripcion = incidencia.getDescripcion();
        if (descripcion != null && !"null".equalsIgnoreCase(descripcion.trim())) {
            textodesc.setText(descripcion);
        }
        TextView textoproc = binding.textProcedimiento;
        String procedimiento = incidencia.getProcedimiento();
        if (procedimiento != null && !"null".equalsIgnoreCase(procedimiento.trim())) {
            textoproc.setText(procedimiento);
        }
        TextView textooperador = binding.textOperador;
        String operador = incidencia.getOperador();
        if (operador != null && !"null".equalsIgnoreCase(operador.trim())) {
            textooperador.setText(textooperador.getText().toString() + incidencia.getOperador());
        }


        return root;


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


    // Método para consultar datos sobre el usuario de la incidencia, enviando la solicitud al servidor
    public void consultar_usuario(){
        final String URL = getString(R.string.IP) + "usuario/" + dni_usuario;

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response != null) {
                                nombre_usuario = response.getString("nombre");
                                apellidos_usuario = response.getString("apellidos");

                                textonombre.setText(nombre_usuario + " " + apellidos_usuario);
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