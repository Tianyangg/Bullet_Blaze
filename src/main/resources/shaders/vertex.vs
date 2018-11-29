#version 330

layout (location = 0) in vec2 position;
layout (location = 1) in vec3 inColour;

out vec3 exColour;

uniform mat4 modelMatrix;

void main()
{
    gl_Position = modelMatrix * vec4(position, 0.0, 1.0);
    exColour = inColour;
}