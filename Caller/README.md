## How to Use?
Clients are expected to communicate with the ServiceMonitor server via console.
Clients can register the name of the service and pollingFrequency to be notified about the status of the service.

## Expected Input
There are 3 types on inputs that ServiceMonitor server expects from a client.
The unit of pollingFrequency is in second.
1. **Add action**: "add [serviceName1]:[pollingFrequency];[serviceName2]:[pollingFrequency2]"
Effects: will replace the existing client's requests on the ServiceMonitor server.
Examples:
```
add a:5;b:7
```

2. **Delete action**: "delete [serviceName1];[serviceName2]"
Effects: let the ServiceMonitor server know that this client is no longer interested in knowing about those services.
Examples:
```
delete a;b
```

3. **Update action**: "update [serviceName1]:[pollingFrequency];[serviceName2]:[pollingFrequency2]"
Effects:  update the pollingFrequency of the specified service name on the ServiceMonitor server
          will start tracking the service name with the pollingFrequency if it has never been requested before.
Examples:
```
add a:5;c:7
```
