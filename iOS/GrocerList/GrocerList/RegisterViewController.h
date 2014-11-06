//
//  RegisterViewController.h
//  GrocerList
//
//  Created by Esau Rubio on 11/5/14.
//  Copyright (c) 2014 Strtatazima. All rights reserved.
//

#import "ViewController.h"

@interface RegisterViewController : ViewController <UITextFieldDelegate>
{
    IBOutlet UITextField *usernameR;
    IBOutlet UITextField *passwordR;
    IBOutlet UITextField *email;
}

-(IBAction)onClick:(id)sender;

@end
