from mpu6050 import mpu6050

class AccModule:
    """
    Module handles the process of getting accelerator data
    and checking for car crash occurrence based on the given
    threshold.
    """

    def __init__(self, thres=0.3):
        # Custom threshold value
        self.thres = thres

        # Initialize the sensor
        self.sensor = mpu6050(0x68)

        # To store previous value
        self.prev = None

    def _reset_sensor(self):
	    try:
	        self.sensor = mpu6050(0x68)
	    except Exception as e:
	        print("Error: "+e)

    def _get_change(self, data):
        # Calculates the change in movement
        change = {"x":0,"y":0,"z":0}
        for pos in data:
            change[pos] = abs(self.prev[pos]-data[pos])

        return change

    def check_crash(self):
        # Get data from the accelerometer
        data = self.sensor.get_accel_data()
        for pos in data:
            if data[pos]==0:
                print("Reseting sensor")
                self._reset_sensor()
                break

        if self.prev is None:
            # Set to current data
            self.prev = data
            return False,data,self.prev

        else:
            change = self._get_change(data)
	    tmp = self.prev
	    self.prev = data
            # Check if any change is greater than threshold
            for pos in change:
                if(change[pos]>=self.thres):
                    return True,data,tmp
	
	return False,data,self.prev
