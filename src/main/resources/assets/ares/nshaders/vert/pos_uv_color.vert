#version 330 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 uv;
layout(location = 2) in vec4 color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertColor;
out vec2 vertUV;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertColor = color;
    vertUV = uv;
}
