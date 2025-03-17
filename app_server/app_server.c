#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <pigpiod_if2.h>

#define PORT 8080
#define BUFFER_SIZE 1024
#define IN1_PIN 23
#define IN2_PIN 24
#define PWM_PIN 18

int stop = 1;
void init_gpio();

int main() {
    int server_fd, client_fd;
    struct sockaddr_in server_addr, client_addr;
    char buffer[BUFFER_SIZE];
    socklen_t addr_len = sizeof(client_addr);
    int pi;
	pi = pigpio_start(NULL, NULL);
	/* socket create */
    server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd == -1) {
	    perror("FAil MAKING SOCKET");
	    exit(1);
    }
    
	/* init socket configure */
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(PORT);

	/* socket bind */
    if (bind(server_fd, (struct sockaddr *)&server_addr, sizeof(server_addr)) == -1) {
	    perror("FAIL BINDING");
	    exit(1);
    }

	/* socket listen */
    if (listen(server_fd, 5) == -1) {
	    perror("FAIL LISTENING");
	    exit(1);
    }

	/* client accpet */
    client_fd = accept(server_fd, (struct sockaddr *)&client_addr, &addr_len);
    if (client_fd == -1) {
	    perror("FAIL client connection");
	    exit(1);
    }

    printf("CLIENT connected: %s\n", inet_ntoa(client_addr.sin_addr));

	/* init gpio */
    init_gpio(pi);

	while(1){    
        /* Read from client socket */
		memset(buffer, 0, BUFFER_SIZE);
	    read(client_fd, buffer, BUFFER_SIZE);
    	printf("RCV MESSAGE: %s\n", buffer);
	    
		/* Control Actuator */
        if(strncmp(buffer, "on", 2) == 0){
			set_PWM_dutycycle(pi, PWM_PIN, 255);
		}

		if(strncmp(buffer, "off", 3) == 0){
			set_PWM_dutycycle(pi, PWM_PIN, 0);
		}

	    if(strncmp(buffer, "open", 4) == 0){
	        gpio_write(pi, IN1_PIN, 1);
		    gpio_write(pi, IN2_PIN, 0);
	    }

	    if(strncmp(buffer,"close", 5) == 0){    
		    gpio_write(pi, IN1_PIN, 0);
		    gpio_write(pi, IN2_PIN, 1);
    	}

	    if(strncmp(buffer,"skoff", 5) == 0){
			pigpio_stop(pi);
		    close(client_fd);
	    	break;
	    }
    }    
    close(server_fd);

    return 0;
}

void init_gpio(int pi){
	if(pi < 0){
		printf("Fail connected pigpio");
	}

	set_mode(pi, IN1_PIN, PI_OUTPUT);
	set_mode(pi, IN2_PIN, PI_OUTPUT);
	set_mode(pi, PWM_PIN, PI_OUTPUT);
	set_PWM_frequency(pi, PWM_PIN, 1000);

}
