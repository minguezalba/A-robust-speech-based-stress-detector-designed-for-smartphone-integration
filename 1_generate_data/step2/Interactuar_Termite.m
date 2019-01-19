
% Envíar datos a puerto COM desde MATLAB
clc
clear all
close all
disp('                          Termite');
disp(' ');

tep = serial('COM6'); % asignar un puerto serie al objeto
set(tep, 'BaudRate', 115200); % configurar el baudrate
set(tep, 'Parity', 'none'); % configurar la paridad
set(tep, 'DataBits', 8); % configurar el databits
set(tep, 'StopBits', 1); % configurar el stopbit

% Mostar las propiedades del objeto al puerto serie en la ventana de
% MATLAB
disp(get(tep,{'Type','Name','Port','BaudRate','Parity','DataBits','StopBits'}));
fopen(tep); 
%data = input('Enter command to start recording:', 's'); % pedir al usuario que introduzca el comando 
data = 'start';
fprintf(tep,data); % enviar el comando introducido al puerto serie
disp('Command sent to Serial Port is:');
disp(data);

% reproducir_audio.m
%data = input('Enter command to finish recording:', 's'); % pedir al usuario que introduzca el comando 
data2 = 'stop';
fprintf(tep,data2); % enviar el comando introducido al puerto serie
disp('Command sent to Serial Port is:');
disp(data);

fclose(tep); % cerrar el objeto al puerto serie