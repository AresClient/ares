#version 330 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec4 color;
layout(location = 2) in vec3 norm;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertColor;
out vec3 vertNorm;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertColor = color;
    vertNorm = norm;
}
