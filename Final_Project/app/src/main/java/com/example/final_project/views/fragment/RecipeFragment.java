package com.example.final_project.views.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.models.entity.Menu;
import com.example.final_project.views.adapter.MenuAdapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MenuAdapter menuAdapter;
    private List<Menu> weeklyMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);

        initViews(view);
        setupRecyclerView();
        loadMenuFromDatabase();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_menu);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        weeklyMenu = new ArrayList<>();
        menuAdapter = new MenuAdapter(weeklyMenu);
        recyclerView.setAdapter(menuAdapter);
    }

    private void loadMenuFromDatabase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<Menu> menuList = new ArrayList<>();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String url = "jdbc:mysql://<HOST>:<PORT>/fridgemanager";
                String user = "<USERNAME>";
                String password = "<PASSWORD>";
                conn = DriverManager.getConnection(url, user, password);
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT menu_id, menu_name, image_url, description, from_date, to_date, create_at, update_at FROM menu");
                while (rs.next()) {
                    Menu menu = new Menu();
                    menu.setMenuId(rs.getString("menu_id"));
                    menu.setMenuName(rs.getString("menu_name"));
                    menu.setImageUrl(rs.getString("image_url"));
                    menu.setDescription(rs.getString("description"));
                    // Có thể parse date nếu cần
                    menuList.add(menu);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception ignored) {}
                try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
                try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            }
            handler.post(() -> {
                weeklyMenu.clear();
                weeklyMenu.addAll(menuList);
                menuAdapter.notifyDataSetChanged();
            });
        });
    }
}
