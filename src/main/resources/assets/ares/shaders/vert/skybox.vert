#version 330 core

layout (location = 0) in vec3 pos;

out vec3 TexCoords;

uniform mat4 projection;
uniform mat4 model;

void main() {
	gl_Position = projection * model * vec4(pos, 1.0);
	TexCoords = pos;
}
