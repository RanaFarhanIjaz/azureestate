"""
predict.py — Quick inference interface for the Housing Price ML model
Usage:
    python ml/predict.py
"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from housing_price_predictor import predict_price

# ── Example properties ──────────────────────────────────────────────────────
examples = [
    dict(
        area=7420, bedrooms=4, bathrooms=2, stories=3,
        mainroad=True, guestroom=False, basement=False,
        hotwaterheating=False, airconditioning=True,
        parking=2, prefarea=True, furnishingstatus="furnished",
        label="Premium 4BHK, AC, furnished, main road"
    ),
    dict(
        area=4000, bedrooms=3, bathrooms=1, stories=2,
        mainroad=True, guestroom=False, basement=False,
        hotwaterheating=False, airconditioning=False,
        parking=1, prefarea=False, furnishingstatus="semi-furnished",
        label="Mid-range 3BHK, semi-furnished"
    ),
    dict(
        area=2500, bedrooms=2, bathrooms=1, stories=1,
        mainroad=False, guestroom=False, basement=False,
        hotwaterheating=False, airconditioning=False,
        parking=0, prefarea=False, furnishingstatus="unfurnished",
        label="Budget 2BHK, unfurnished"
    ),
]

print("\n" + "=" * 65)
print("  Azure Estate — Housing Price Predictions")
print("=" * 65)

for ex in examples:
    label = ex.pop("label")
    result = predict_price(**ex)
    print(f"\n  {label}")
    print(f"  Predicted Price : Rs. {result['predicted_price']:>15,.0f}")
    print(f"  Model           : {result['model_name']}  (Test R2={result['r2_test']})")

print("\n" + "=" * 65)
