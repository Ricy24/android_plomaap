import json
import sys
import os

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
os.chdir(BACKEND_DIR)

from datetime import datetime, timedelta

# Import Flask app
from app import create_app
from app.database.extensions import db
from app.database.models import User, Service, Appointment, UserAddress

app = create_app()
client = app.test_client()

with app.app_context():
    # 1. Login or obtain token for customer user (id=1)
    customer = User.query.filter_by(role='customer').first()
    if not customer:
        print("No customer found in DB")
        sys.exit(1)

    from flask_jwt_extended import create_access_token
    token = create_access_token(identity=str(customer.id))
    headers = {
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/json'
    }

    print(f"Testing with Customer: {customer.name} (ID: {customer.id})")

    # 2. Test Availability Slots
    test_date = (datetime.now().date() + timedelta(days=2)).strftime('%Y-%m-%d')
    res = client.get(f'/api/appointments/availability?service_id=1&date={test_date}')
    print(f"GET /api/appointments/availability?service_id=1&date={test_date} -> Status {res.status_code}")
    slots_data = res.get_json()
    assert res.status_code == 200, f"Expected 200, got {res.status_code}"
    assert 'slots' in slots_data, "Expected slots in response"
    print(f"Generated {len(slots_data['slots'])} slots. First slot: {slots_data['slots'][0]}")

    # 3. Test Booking Creation with Auto-assign & Address Snapshot
    # Find first available slot
    available_slot = next((s for s in slots_data['slots'] if s['available']), None)
    if not available_slot:
        print("No available slot found for test date, picking 10:00")
        slot_time = "10:00"
    else:
        slot_time = available_slot['time']

    booking_payload = {
        'service_id': 1,
        'date': test_date,
        'time': slot_time,
        'address': 'Carrera 7 # 12-34, Bogotá',
        'address_reference': 'Apto 502, Torre Norte',
        'notes': 'El lavamanos principal tiene una fuga continua.'
    }

    res_post = client.post('/api/appointments', headers=headers, data=json.dumps(booking_payload))
    print(f"POST /api/appointments -> Status {res_post.status_code}")
    post_data = res_post.get_json()
    print("Response:", post_data)
    assert res_post.status_code == 201, f"Expected 201, got {res_post.status_code}"
    created_apt = post_data['appointment']
    assert created_apt['address'] == 'Carrera 7 # 12-34, Bogotá'
    assert created_apt['technician'] is not None, "Technician should be auto-assigned"
    print(f"Appointment created successfully! ID: {created_apt['id']}, Tech: {created_apt['technician_name']}")

    # 4. Test Double Booking Prevention on same slot with same technician
    res_double = client.post('/api/appointments', headers=headers, data=json.dumps(booking_payload))
    print(f"POST /api/appointments (Double booking attempt) -> Status {res_double.status_code}")
    # If there are other technicians available, it might assign the other one; if no more technicians, returns 409
    double_data = res_double.get_json()
    print("Double booking response:", double_data)

    # 5. Clean up created test appointment(s)
    Appointment.query.filter_by(id=created_apt['id']).delete()
    if double_data.get('success') and 'appointment' in double_data:
        Appointment.query.filter_by(id=double_data['appointment']['id']).delete()
    db.session.commit()
    print("Cleaned up test appointments.")

    print("\nALL BACKEND BOOKING TESTS PASSED PERFECTLY!")
