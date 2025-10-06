package com.example.assignment_task1.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.assignment_task1.R;
import com.example.assignment_task1.adapter.MenuAdapter;
import com.example.assignment_task1.model.DayMenu;
import com.example.assignment_task1.model.Recipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MenuAdapter menuAdapter;
    private List<DayMenu> weeklyMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);

        initViews(view);
        setupRecyclerView();
        loadMockData();

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

    private void loadMockData() {
        // Tạo mock data cho menu tuần
        weeklyMenu.clear();

        // Thứ 2
        List<Recipe> mondayRecipes = Arrays.asList(
            new Recipe("Phở Bò", "Phở bò truyền thống với nước dùng đậm đà", "", "pho_bo", "pho_bo_2", "pho_bo_3", "Phở bò truyền thống, nước dùng đậm đà"),
            new Recipe("Bún Chả", "Bún chả Hà Nội với thịt nướng thơm ngon", "", "bun_cha", "bun_cha_2", "bun_cha_3", "Thịt nướng, bún, rau sống"),
            new Recipe("Chè Đậu Xanh", "Chè đậu xanh mát lạnh", "", "che_dau_xanh", "che_dau_xanh_2", "che_dau_xanh_3", "Chè đậu xanh, nước cốt dừa")
        );
        weeklyMenu.add(new DayMenu("Thứ Hai", mondayRecipes));

        // Thứ 3
        List<Recipe> tuesdayRecipes = Arrays.asList(
            new Recipe("Cơm Tấm", "Cơm tấm sườn nướng", "", "com_tam", "com_tam_2", "com_tam_3", "Cơm tấm, sườn nướng, mỡ hành"),
            new Recipe("Bánh Mì", "Bánh mì thịt nướng", "", "banh_mi", "banh_mi_2", "banh_mi_3", "Bánh mì, thịt nướng, rau củ"),
            new Recipe("Sinh Tố", "Sinh tố bơ sáp", "", "sinh_to", "sinh_to_2", "sinh_to_3", "Sinh tố bơ, sữa đặc")
        );
        weeklyMenu.add(new DayMenu("Thứ Ba", tuesdayRecipes));

        // Thứ 4
        List<Recipe> wednesdayRecipes = Arrays.asList(
            new Recipe("Bún Bò Huế", "Bún bò Huế cay nồng", "", "bun_bo_hue", "bun_bo_hue_2", "bun_bo_hue_3", "Bún bò Huế, chả cua, rau thơm"),
            new Recipe("Nem Nướng", "Nem nướng Nha Trang", "", "nem_nuong", "nem_nuong_2", "nem_nuong_3", "Nem nướng, bánh hỏi, rau sống"),
            new Recipe("Trà Sữa", "Trà sữa trân châu", "", "tra_sua", "tra_sua_2", "tra_sua_3", "Trà sữa, trân châu")
        );
        weeklyMenu.add(new DayMenu("Thứ Tư", wednesdayRecipes));

        // Thứ 5
        List<Recipe> thursdayRecipes = Arrays.asList(
            new Recipe("Mì Quảng", "Mì Quảng đặc sản", "", "mi_quang", "mi_quang_2", "mi_quang_3", "Mì Quảng, tôm, thịt, rau sống"),
            new Recipe("Bánh Xèo", "Bánh xèo miền Nam", "", "banh_xeo", "banh_xeo_2", "banh_xeo_3", "Bánh xèo, tôm, thịt, giá"),
            new Recipe("Nước Mía", "Nước mía tươi", "", "nuoc_mia", "nuoc_mia_2", "nuoc_mia_3", "Nước mía, tắc")
        );
        weeklyMenu.add(new DayMenu("Thứ Năm", thursdayRecipes));

        // Thứ 6
        List<Recipe> fridayRecipes = Arrays.asList(
            new Recipe("Hủ Tiếu", "Hủ tiếu Nam Vang", "", "hu_tieu", "hu_tieu_2", "hu_tieu_3", "Hủ tiếu, tôm, thịt, trứng cút"),
            new Recipe("Bánh Canh", "Bánh canh cua", "", "banh_canh", "banh_canh_2", "banh_canh_3", "Bánh canh cua, hành ngò"),
            new Recipe("Cà Phê", "Cà phê sữa đá", "", "ca_phe", "ca_phe_2", "ca_phe_3", "Cà phê, sữa đặc")
        );
        weeklyMenu.add(new DayMenu("Thứ Sáu", fridayRecipes));

        // Thứ 7
        List<Recipe> saturdayRecipes = Arrays.asList(
            new Recipe("Bánh Cuốn", "Bánh cuốn Hà Nội", "", "banh_cuon", "banh_cuon_2", "banh_cuon_3", "Bánh cuốn, chả, rau thơm"),
            new Recipe("Chả Cá", "Chả cá Lã Vọng", "", "cha_ca", "cha_ca_2", "cha_ca_3", "Chả cá, thì là, rau thơm"),
            new Recipe("Kem", "Kem tràng tiền", "", "kem", "kem_2", "kem_3", "Kem, đậu phộng")
        );
        weeklyMenu.add(new DayMenu("Thứ Bảy", saturdayRecipes));

        // Chủ Nhật
        List<Recipe> sundayRecipes = Arrays.asList(
            new Recipe("Bánh Chưng", "Bánh chưng truyền thống", "", "banh_chung", "banh_chung_2", "banh_chung_3", "Bánh chưng, thịt mỡ, đậu xanh"),
            new Recipe("Thịt Kho Tàu", "Thịt kho tàu đậm đà", "", "thit_kho_tau", "thit_kho_tau_2", "thit_kho_tau_3", "Thịt kho tàu, trứng, nước dừa"),
            new Recipe("Chè Cung Đình", "Chè cung đình Huế", "", "che_cung_dinh", "che_cung_dinh_2", "che_cung_dinh_3", "Chè cung đình, hạt sen")
        );
        weeklyMenu.add(new DayMenu("Chủ Nhật", sundayRecipes));

        android.util.Log.d("RecipeFragment", "Loaded " + weeklyMenu.size() + " days");
        for (DayMenu dayMenu : weeklyMenu) {
            android.util.Log.d("RecipeFragment", dayMenu.getDayName() + " has " + dayMenu.getRecipes().size() + " recipes");
        }

        menuAdapter.notifyDataSetChanged();
    }
}
