"""
Housing Price Predictor — Azure Estate ML Module
================================================
Dataset  : Housing.csv  (546 rows, 13 columns)
Target   : price
Algorithm: Multiple Linear Regression (+ Ridge / Lasso comparison)
Output   : housing_model.pkl  — serialised pipeline ready for inference
"""

import os, sys, json, pickle
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")          # headless – no GUI needed
import matplotlib.pyplot as plt

from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.linear_model import LinearRegression, Ridge, Lasso
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.pipeline import Pipeline

# ─────────────────────────────────────────────────────────────
# 1.  PATHS
# ─────────────────────────────────────────────────────────────
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT_DIR   = os.path.dirname(SCRIPT_DIR)           # azureestate/
CSV_PATH   = os.path.join(ROOT_DIR, "Housing.csv")
MODEL_PATH = os.path.join(SCRIPT_DIR, "housing_model.pkl")
META_PATH  = os.path.join(SCRIPT_DIR, "model_meta.json")
PLOT_DIR   = os.path.join(SCRIPT_DIR, "plots")
os.makedirs(PLOT_DIR, exist_ok=True)

# ─────────────────────────────────────────────────────────────
# 2.  LOAD DATA
# ─────────────────────────────────────────────────────────────
print("=" * 60)
print("  Azure Estate — Housing Price Predictor (Linear Regression)")
print("=" * 60)
df = pd.read_csv(CSV_PATH)
print(f"\n[DATA]  Loaded {len(df)} rows × {len(df.columns)} columns")
print(df.head(3).to_string())

# ─────────────────────────────────────────────────────────────
# 3.  PREPROCESSING
# ─────────────────────────────────────────────────────────────
# Binary yes/no columns → 1/0
binary_cols = [
    "mainroad", "guestroom", "basement",
    "hotwaterheating", "airconditioning", "prefarea"
]
for col in binary_cols:
    df[col] = (df[col].str.strip().str.lower() == "yes").astype(int)

# furnishingstatus → ordinal  (unfurnished=0, semi-furnished=1, furnished=2)
furnish_map = {"unfurnished": 0, "semi-furnished": 1, "furnished": 2}
df["furnishingstatus"] = (
    df["furnishingstatus"].str.strip().str.lower().map(furnish_map)
)

print(f"\n[PREP]  Encoding done — no missing values: {df.isnull().sum().sum() == 0}")

# ─────────────────────────────────────────────────────────────
# 4.  FEATURE / TARGET SPLIT
# ─────────────────────────────────────────────────────────────
FEATURES = [
    "area", "bedrooms", "bathrooms", "stories",
    "mainroad", "guestroom", "basement", "hotwaterheating",
    "airconditioning", "parking", "prefarea", "furnishingstatus"
]
TARGET = "price"

X = df[FEATURES]
y = df[TARGET]

print(f"\n[SPLIT] Features: {FEATURES}")
print(f"        Target  : {TARGET}  (min={y.min():,.0f}  max={y.max():,.0f}  mean={y.mean():,.0f})")

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.20, random_state=42
)
print(f"        Train={len(X_train)}  Test={len(X_test)}")

# ─────────────────────────────────────────────────────────────
# 5.  BUILD & COMPARE MODELS
# ─────────────────────────────────────────────────────────────
models = {
    "Linear Regression": LinearRegression(),
    "Ridge (a=1.0)"    : Ridge(alpha=1.0),
    "Lasso (a=1000)"   : Lasso(alpha=1000, max_iter=10000),
}

results = {}
print("\n" + "-" * 60)
print(f"{'Model':<22}  {'R2':>7}  {'MAE':>14}  {'RMSE':>14}")
print("-" * 60)

best_name, best_pipe, best_r2 = None, None, -np.inf

for name, estimator in models.items():
    pipe = Pipeline([
        ("scaler", StandardScaler()),
        ("model",  estimator),
    ])
    pipe.fit(X_train, y_train)
    y_pred = pipe.predict(X_test)

    r2   = r2_score(y_test, y_pred)
    mae  = mean_absolute_error(y_test, y_pred)
    rmse = np.sqrt(mean_squared_error(y_test, y_pred))

    results[name] = {"r2": r2, "mae": mae, "rmse": rmse}
    print(f"{name:<22}  {r2:>7.4f}  {mae:>14,.0f}  {rmse:>14,.0f}")

    if r2 > best_r2:
        best_r2, best_name, best_pipe = r2, name, pipe

print("-" * 60)
print(f"\n[BEST]  {best_name}  ->  R2 = {best_r2:.4f}")

# ─────────────────────────────────────────────────────────────
# 6.  CROSS-VALIDATION (best model)
# ─────────────────────────────────────────────────────────────
cv_scores = cross_val_score(best_pipe, X, y, cv=5, scoring="r2")
print(f"\n[CV]    5-fold R² scores : {cv_scores.round(4)}")
print(f"        Mean CV R2       : {cv_scores.mean():.4f} +/- {cv_scores.std():.4f}")

# ─────────────────────────────────────────────────────────────
# 7.  FEATURE IMPORTANCE  (coefficients of the linear model)
# ─────────────────────────────────────────────────────────────
lr_pipe = Pipeline([("scaler", StandardScaler()), ("model", LinearRegression())])
lr_pipe.fit(X_train, y_train)
coefs = lr_pipe.named_steps["model"].coef_
feat_imp = pd.Series(coefs, index=FEATURES).sort_values(key=abs, ascending=False)
print("\n[COEF]  Feature coefficients (LinearRegression, standardised):")
for feat, val in feat_imp.items():
    print(f"        {feat:<20} {val:>+15,.0f}")

# ─────────────────────────────────────────────────────────────
# 8.  PLOTS
# ─────────────────────────────────────────────────────────────
# 8a — Actual vs Predicted
y_pred_best = best_pipe.predict(X_test)
fig, axes = plt.subplots(1, 2, figsize=(14, 5))
fig.suptitle("Housing Price Prediction — Azure Estate ML", fontsize=13, fontweight="bold")

ax = axes[0]
ax.scatter(y_test / 1e6, y_pred_best / 1e6, alpha=0.6, edgecolors="k", linewidths=0.4, color="#3DB8A8")
mn, mx = y_test.min() / 1e6, y_test.max() / 1e6
ax.plot([mn, mx], [mn, mx], "r--", linewidth=1.5, label="Perfect fit")
ax.set_xlabel("Actual Price (M)")
ax.set_ylabel("Predicted Price (M)")
ax.set_title(f"Actual vs Predicted\n{best_name}  |  R²={best_r2:.3f}")
ax.legend()

# 8b — Feature importance bar chart
ax2 = axes[1]
colors = ["#3DB8A8" if v > 0 else "#E53935" for v in feat_imp.values]
feat_imp.plot(kind="barh", ax=ax2, color=colors)
ax2.set_title("Feature Coefficients\n(standardised inputs)")
ax2.set_xlabel("Coefficient value")
ax2.axvline(0, color="black", linewidth=0.8)

plt.tight_layout()
plot_path = os.path.join(PLOT_DIR, "model_results.png")
plt.savefig(plot_path, dpi=150)
print(f"\n[PLOT]  Saved -> {plot_path}")

# ─────────────────────────────────────────────────────────────
# 9.  SAVE MODEL + METADATA
# ─────────────────────────────────────────────────────────────
with open(MODEL_PATH, "wb") as f:
    pickle.dump(best_pipe, f)
print(f"[SAVE]  Model  -> {MODEL_PATH}")

meta = {
    "model_name"   : best_name,
    "features"     : FEATURES,
    "target"       : TARGET,
    "r2_test"      : round(best_r2, 4),
    "mae_test"     : round(results[best_name]["mae"], 2),
    "rmse_test"    : round(results[best_name]["rmse"], 2),
    "cv_r2_mean"   : round(float(cv_scores.mean()), 4),
    "cv_r2_std"    : round(float(cv_scores.std()), 4),
    "furnish_map"  : furnish_map,
    "binary_cols"  : binary_cols,
}
with open(META_PATH, "w") as f:
    json.dump(meta, f, indent=2)
print(f"[SAVE]  Meta   -> {META_PATH}")

# ─────────────────────────────────────────────────────────────
# 10. INFERENCE HELPER  (call from anywhere)
# ─────────────────────────────────────────────────────────────
def predict_price(
    area: int,
    bedrooms: int,
    bathrooms: int,
    stories: int,
    mainroad: bool,
    guestroom: bool,
    basement: bool,
    hotwaterheating: bool,
    airconditioning: bool,
    parking: int,
    prefarea: bool,
    furnishingstatus: str,   # "furnished" | "semi-furnished" | "unfurnished"
    model_path: str = MODEL_PATH,
    meta_path:  str = META_PATH,
) -> dict:
    """
    Predict housing price for a single property.

    Returns
    -------
    dict with keys: predicted_price (float), model_name (str), r2_test (float)
    """
    with open(model_path, "rb") as f:
        pipe = pickle.load(f)
    with open(meta_path) as f:
        meta = json.load(f)

    fmap = meta["furnish_map"]
    fstatus = fmap.get(furnishingstatus.lower().strip(), 1)

    row = pd.DataFrame([{
        "area"            : area,
        "bedrooms"        : bedrooms,
        "bathrooms"       : bathrooms,
        "stories"         : stories,
        "mainroad"        : int(mainroad),
        "guestroom"       : int(guestroom),
        "basement"        : int(basement),
        "hotwaterheating" : int(hotwaterheating),
        "airconditioning" : int(airconditioning),
        "parking"         : parking,
        "prefarea"        : int(prefarea),
        "furnishingstatus": fstatus,
    }])

    predicted = float(pipe.predict(row)[0])
    return {
        "predicted_price": round(predicted, 2),
        "model_name"     : meta["model_name"],
        "r2_test"        : meta["r2_test"],
    }


# ─────────────────────────────────────────────────────────────
# 11. QUICK DEMO PREDICTION
# ─────────────────────────────────────────────────────────────
if __name__ == "__main__":
    demo = predict_price(
        area=7420, bedrooms=4, bathrooms=2, stories=3,
        mainroad=True, guestroom=False, basement=False,
        hotwaterheating=False, airconditioning=True,
        parking=2, prefarea=True, furnishingstatus="furnished",
    )
    print(f"\n{'='*60}")
    print(f"  DEMO PREDICTION")
    print(f"  Property: 7420 sqft, 4 bed, 2 bath, 3 stories, AC, furnished")
    print(f"  Predicted Price: Rs. {demo['predicted_price']:>15,.0f}")
    print(f"  Model           : {demo['model_name']}")
    print(f"  Test R2         : {demo['r2_test']}")
    print(f"{'='*60}")
