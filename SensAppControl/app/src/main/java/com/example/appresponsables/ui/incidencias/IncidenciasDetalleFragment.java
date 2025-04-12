// Este archivo define el fragmento `IncidenciasDetalleFragment`.
// Permite a los usuarios responsables consultar los detalles de una incidencia concreta.



package com.example.appresponsables.ui.incidencias;

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
import com.example.appresponsables.Incidencia;
import com.example.appresponsables.R;
import com.example.appresponsables.databinding.FragmentDetalleIncidenciasBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.LocalTime;



public class IncidenciasDetalleFragment extends Fragment {
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
    private NavController navController;
    private FragmentDetalleIncidenciasBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentDetalleIncidenciasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_responsables);
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
                Log.e("IncidenciasDetalleFragment", "El string JSON es null");
            }
        }

        // Configuramos los datos de la incidencia en pantalla
        if(incidencia.getResuelta()){
            binding.linearLayoutId.setBackgroundResource(R.color.resuelta);
        }else{
            binding.linearLayoutId.setBackgroundResource(R.color.noresuelta);
        }
        TextView  textoid= binding.textId;
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



    }


