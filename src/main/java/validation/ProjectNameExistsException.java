/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

/**
 *
 * @author Stas
 */
public class ProjectNameExistsException extends Throwable {
    public ProjectNameExistsException(String message) {
        super(message);
    }
}