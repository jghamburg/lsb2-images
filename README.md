# Learning Springboot 2: Chat Application  

This component provides an image server providing the means to save and retrieve images.

## Migrate to k8s  

For the migration to kubernetes I use the gradle plugin `xxx`.  
To setup helm chart definition do  

```
cd src/main
helm create lsbs2-images
mv lsb2-images helm
cd ../..
```

In addition a few changes must be applied to the existing templates.  

`deployment.yaml`:  
```
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: {{ .Values.app.port }}
              protocol: TCP
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: http
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: http
```

Add definition of default port for web application and add support of Spring Actuator endpoint to use for health checks.  

Update `Notes.txt`:  

```
  kubectl port-forward $POD_NAME 8080:{{ .Values.app.port }}
{{- end }}
```

to use default application port inside of pod definition.  


