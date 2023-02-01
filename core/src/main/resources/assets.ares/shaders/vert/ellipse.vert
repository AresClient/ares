#version 330 core

layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 uv;
layout (location = 2) in vec4 color;

uniform mat4 projection;
uniform mat4 model;

out vec2 vertUV;
out vec4 vertColor;

void main() {
    gl_Position = projection * model * vec4(pos, 1.0);
    vertUV = uv;
    vertColor = color;
}
