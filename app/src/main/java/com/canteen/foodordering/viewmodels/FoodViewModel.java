package com.canteen.foodordering.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.repositories.FoodRepository;

import java.util.List;

public class FoodViewModel extends AndroidViewModel {
    private final FoodRepository foodRepository;

    public FoodViewModel(@NonNull Application application) {
        super(application);
        foodRepository = new FoodRepository();
    }

    public LiveData<List<FoodItem>> getFoodItemsLiveData() {
        return foodRepository.getFoodItemsLiveData();
    }

    public LiveData<String> getStatusMessageLiveData() {
        return foodRepository.getStatusMessageLiveData();
    }

    public void addFoodItem(FoodItem foodItem) {
        foodRepository.addFoodItem(foodItem);
    }

    public void updateFoodItem(FoodItem foodItem) {
        foodRepository.updateFoodItem(foodItem);
    }

    public void deleteFoodItem(String foodId) {
        foodRepository.deleteFoodItem(foodId);
    }

    public void toggleFavorite(String foodId, boolean currentFavorite) {
        foodRepository.toggleFavorite(foodId, currentFavorite);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        foodRepository.detachListener();
    }
}
