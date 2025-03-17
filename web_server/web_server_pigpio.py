from flask import Flask, render_template, url_for, redirect
import pigpio

in_pin_dict = {'IN1': 23, 'IN2' : 24, 'PWM' : 18}
in_state_dict = {'IN1' : 0, 'IN2':0, 'PWM': 0}

pi = pigpio.pi()
if not pi.connected:
    exit()
    
pi.set_mode(in_pin_dict['IN1'], pigpio.OUTPUT)
pi.set_mode(in_pin_dict['IN2'], pigpio.OUTPUT)
pi.set_mode(in_pin_dict['PWM'], pigpio.OUTPUT)
pi.set_PWM_frequency(in_pin_dict['PWM'], 3000)

app = Flask(__name__)

@app.route('/')
def home():
    return render_template('web_actuator.html', in_state_dict = in_state_dict)

@app.route('/oof/<string:onf>')
def OOF(onf):
    if onf == 'on':
        in_state_dict['PWM'] = 255
    elif onf == 'off':
        in_state_dict['PWM'] = 0
        
    pi.set_PWM_dutycycle(in_pin_dict['PWM'], in_state_dict['PWM'])
    return redirect(url_for('home'))

@app.route('/ooc/<string:state>')
def OOC(state):
    if state == 'open':
        in_state_dict['IN1'] = 1
        in_state_dict['IN2'] = 0
    elif state == 'close':
        in_state_dict['IN1'] = 0
        in_state_dict['IN2'] = 1
        
    pi.write(in_pin_dict['IN1'],in_state_dict['IN1'])
    pi.write(in_pin_dict['IN2'],in_state_dict['IN2'])
    return redirect(url_for('home'))

if __name__ == "__main__":
    app.run(host="192.168.0.234", port = 8000)